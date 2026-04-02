package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.dto.ChatRequestDTO;
import com.ruoyi.ilightmate.service.IIlmAiProxyService;
import com.ruoyi.ilightmate.service.IIlmTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * AI 对话代理 Controller
 *
 * 代理前端 AI 请求到 Claude / MiniMax API
 * 支持 SSE 流式响应
 * 服务端权威 Token 计量
 */
@RestController
@RequestMapping("/system/ai")
public class IlmAiChatController {

    private static final Logger log = LoggerFactory.getLogger(IlmAiChatController.class);

    @Autowired
    private IIlmAiProxyService aiProxyService;

    @Autowired
    private IIlmTokenService tokenService;

    /**
     * AI 对话（SSE 流式）
     *
     * 1. 检查 token 额度
     * 2. 转发到 AI 提供商
     * 3. SSE 流式返回
     * 4. 计入 token 使用量
     */
    @PostMapping("/chat")
    public void chat(@RequestBody ChatRequestDTO req, HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.getUserId();

        // 1. 检查 token 额度
        if (tokenService.isMonthlyLimitReached(userId)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":429,\"msg\":\"Token额度已用尽，请升级或购买加购包\"}");
            writer.flush();
            return;
        }

        // 2. 检查每日对话上限
        if (tokenService.isDailyDialogueLimitReached(userId)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":429,\"msg\":\"今日对话次数已用完，明天再来\"}");
            writer.flush();
            return;
        }

        // 3. SSE 流式响应
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        try {
            // 4. 代理 AI 请求并流式转发
            int tokensUsed = aiProxyService.streamChat(req, response.getWriter(), userId);

            // 5. 计入 token 使用量
            tokenService.incrementTokenUsage(userId, tokensUsed);
            tokenService.incrementDialogueCount(userId);

        } catch (Exception e) {
            log.error("AI chat error for user {}: {}", userId, e.getMessage());
            PrintWriter writer = response.getWriter();
            writer.write("data: {\"error\":\"服务暂时不可用，请稍后重试\"}\n\n");
            writer.write("data: [DONE]\n\n");
            writer.flush();
        }
    }

    /**
     * AI 对话（非流式，用于兼容）
     */
    @PostMapping("/chat/sync")
    public AjaxResult chatSync(@RequestBody ChatRequestDTO req) {
        Long userId = SecurityUtils.getUserId();

        if (tokenService.isMonthlyLimitReached(userId)) {
            return AjaxResult.error(429, "Token额度已用尽");
        }

        try {
            String content = aiProxyService.syncChat(req, userId);
            int tokensUsed = aiProxyService.estimateTokens(content);
            tokenService.incrementTokenUsage(userId, tokensUsed);
            tokenService.incrementDialogueCount(userId);

            AjaxResult result = AjaxResult.success();
            result.put("content", content);
            return result;
        } catch (Exception e) {
            log.error("AI sync chat error: {}", e.getMessage());
            return AjaxResult.error("AI 服务暂时不可用");
        }
    }
}
