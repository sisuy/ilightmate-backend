package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmDialogueHistory {
    private Long id;
    private Long userId;
    private String companionId;
    private String conversationSummary;
    private String keyDiscoveries;       // JSON
    private String emotionalTrajectory;
    private String commitments;          // JSON
    private Integer messageCount;
    private Integer duration;            // milliseconds
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCompanionId() { return companionId; }
    public void setCompanionId(String companionId) { this.companionId = companionId; }
    public String getConversationSummary() { return conversationSummary; }
    public void setConversationSummary(String conversationSummary) { this.conversationSummary = conversationSummary; }
    public String getKeyDiscoveries() { return keyDiscoveries; }
    public void setKeyDiscoveries(String keyDiscoveries) { this.keyDiscoveries = keyDiscoveries; }
    public String getEmotionalTrajectory() { return emotionalTrajectory; }
    public void setEmotionalTrajectory(String emotionalTrajectory) { this.emotionalTrajectory = emotionalTrajectory; }
    public String getCommitments() { return commitments; }
    public void setCommitments(String commitments) { this.commitments = commitments; }
    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
