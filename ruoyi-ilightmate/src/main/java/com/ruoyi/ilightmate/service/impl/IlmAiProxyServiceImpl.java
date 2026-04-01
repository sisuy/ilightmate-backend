package com.ruoyi.ilightmate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ilightmate.config.AiProviderConfig;
import com.ruoyi.ilightmate.dto.ChatRequestDTO;
import com.ruoyi.ilightmate.service.IIlmAiProxyService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 代理服务实现
 *
 * 支持 Claude 和 MiniMax/OpenAI 兼容格式
 * SSE 流式转发：逐行读取 AI 响应，实时写给前端
 */
@Service
public class IlmAiProxyServiceImpl implements IIlmAiProxyService {

    private static final Logger log = LoggerFactory.getLogger(IlmAiProxyServiceImpl.class);

    @Autowired
    private AiProviderConfig config;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public int streamChat(ChatRequestDTO req, PrintWriter writer, Long userId) throws Exception {
        String provider = config.getProvider();
        Request httpReq = buildRequest(req, provider, true);

        int totalTokens = 0;

        try (Response response = httpClient.newCall(httpReq).execute()) {
            if (!response.isSuccessful()) {
                writer.write("data: {\"error\":\"AI 服务返回错误: " + response.code() + "\"}\n\n");
                writer.flush();
                return 0;
            }

            ResponseBody body = response.body();
            if (body == null) return 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream()))) {
                String line;
                StringBuilder fullContent = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        if ("[DONE]".equals(data)) {
                            writer.write("data: [DONE]\n\n");
                            writer.flush();
                            break;
                        }

                        try {
                            JSONObject json = JSON.parseObject(data);
                            String content = extractContent(json, provider);

                            if (content != null && !content.isEmpty()) {
                                fullContent.append(content);
                                // 转发给前端（保持前端期望的格式）
                                JSONObject forward = new JSONObject();
                                forward.put("content", content);
                                writer.write("data: " + forward.toJSONString() + "\n\n");
                                writer.flush();
                            }

                            // 提取 usage 信息（通常在最后一条）
                            if (json.containsKey("usage")) {
                                JSONObject usage = json.getJSONObject("usage");
                                if (usage != null && usage.containsKey("total_tokens")) {
                                    totalTokens = usage.getIntValue("total_tokens");
                                }
                            }
                        } catch (Exception e) {
                            // 解析失败的行跳过
                            log.debug("Skip unparseable SSE line: {}", data);
                        }
                    }
                }

                // 如果 AI 没返回 usage，用估算
                if (totalTokens == 0) {
                    totalTokens = estimateTokens(fullContent.toString());
                    // 加上 input tokens 的粗略估算
                    int inputEstimate = req.getMessages().stream()
                            .mapToInt(m -> estimateTokens(m.getContent()))
                            .sum();
                    totalTokens += inputEstimate;
                }
            }
        }

        return totalTokens;
    }

    @Override
    public String syncChat(ChatRequestDTO req, Long userId) throws Exception {
        String provider = config.getProvider();
        Request httpReq = buildRequest(req, provider, false);

        try (Response response = httpClient.newCall(httpReq).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("AI service error: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) throw new RuntimeException("Empty AI response");

            String responseBody = body.string();
            JSONObject json = JSON.parseObject(responseBody);

            if ("claude".equals(provider)) {
                return json.getJSONArray("content").getJSONObject(0).getString("text");
            } else {
                return json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content");
            }
        }
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        // 粗略估算：中文约 1.5 token/字，英文约 0.75 token/word
        int chineseChars = 0;
        int asciiChars = 0;
        for (char c : text.toCharArray()) {
            if (c > 127) chineseChars++;
            else asciiChars++;
        }
        return (int) (chineseChars * 1.5 + asciiChars * 0.25);
    }

    // ── 内部方法 ──

    private Request buildRequest(ChatRequestDTO req, String provider, boolean stream) {
        Map<String, Object> body = new HashMap<>();

        if ("claude".equals(provider)) {
            body.put("model", config.getModelId());
            body.put("max_tokens", config.getMaxTokens());
            body.put("stream", stream);

            // Claude 格式：system 单独提取
            String systemPrompt = req.getMessages().stream()
                    .filter(m -> "system".equals(m.getRole()))
                    .map(ChatRequestDTO.Message::getContent)
                    .collect(Collectors.joining("\n"));
            List<Map<String, String>> messages = req.getMessages().stream()
                    .filter(m -> !"system".equals(m.getRole()))
                    .map(m -> {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", m.getRole());
                        msg.put("content", m.getContent());
                        return msg;
                    }).collect(Collectors.toList());

            body.put("system", systemPrompt);
            body.put("messages", messages);

            return new Request.Builder()
                    .url(config.getApiUrl())
                    .addHeader("x-api-key", config.getApiKey())
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(body), MediaType.parse("application/json")))
                    .build();

        } else {
            // MiniMax / OpenAI 兼容格式
            body.put("model", config.getModelId());
            body.put("max_tokens", config.getMaxTokens());
            body.put("temperature", config.getTemperature());
            body.put("stream", stream);

            List<Map<String, String>> messages = req.getMessages().stream()
                    .map(m -> {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", m.getRole());
                        msg.put("content", m.getContent());
                        return msg;
                    }).collect(Collectors.toList());
            body.put("messages", messages);

            return new Request.Builder()
                    .url(config.getApiUrl())
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON.toJSONString(body), MediaType.parse("application/json")))
                    .build();
        }
    }

    private String extractContent(JSONObject json, String provider) {
        try {
            if ("claude".equals(provider)) {
                // Claude streaming: {"type":"content_block_delta","delta":{"type":"text_delta","text":"..."}}
                if ("content_block_delta".equals(json.getString("type"))) {
                    JSONObject delta = json.getJSONObject("delta");
                    return delta != null ? delta.getString("text") : null;
                }
            } else {
                // OpenAI/MiniMax: {"choices":[{"delta":{"content":"..."}}]}
                var choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                    return delta != null ? delta.getString("content") : null;
                }
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        return null;
    }
}
