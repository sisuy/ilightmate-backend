package com.ruoyi.ilightmate.dto;

import java.util.List;
import java.util.Map;

/**
 * AI 对话请求 DTO — 匹配前端 src/api/ai.ts 的 ChatRequest
 */
public class ChatRequestDTO {

    /** AI 模型标识（默认 "default"，由后端根据 admin 配置选择） */
    private String model;

    /** 对话消息列表 */
    private List<Message> messages;

    /** 伙伴 ID（yu/sophie/ming/qing/marcus/leanne） */
    private String companionId;

    /** 可选：是否要求流式响应 */
    private Boolean stream;

    /** 可选：用户上下文（近期情绪、七维分数等） */
    private Map<String, Object> context;

    public static class Message {
        private String role;    // "system" | "user" | "assistant"
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public String getCompanionId() { return companionId; }
    public void setCompanionId(String companionId) { this.companionId = companionId; }
    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
