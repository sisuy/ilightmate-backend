package com.ruoyi.ilightmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 提供商配置
 *
 * 在 application.yml 中配置：
 * ilightmate:
 *   ai:
 *     provider: minimax       # claude / minimax / openai
 *     api-url: https://api.minimax.io/v1/chat/completions
 *     api-key: ${AI_API_KEY}
 *     model-id: MiniMax-Text-01
 *     max-tokens: 4096
 *     temperature: 0.7
 */
@Component
@ConfigurationProperties(prefix = "ilightmate.ai")
public class AiProviderConfig {

    /** AI 提供商：claude / minimax / openai */
    private String provider = "minimax";

    /** API 地址 */
    private String apiUrl = "https://api.minimax.io/v1/chat/completions";

    /** API Key（从环境变量读取） */
    private String apiKey;

    /** 模型 ID */
    private String modelId = "MiniMax-Text-01";

    /** 最大 token 数 */
    private int maxTokens = 4096;

    /** 温度参数 */
    private double temperature = 0.7;

    // Getters and Setters
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
}
