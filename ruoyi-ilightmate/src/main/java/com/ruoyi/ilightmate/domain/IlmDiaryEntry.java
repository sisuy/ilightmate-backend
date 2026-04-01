package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmDiaryEntry {
    private Long id;
    private Long userId;
    private String content;
    private Integer mood;
    private String emotions;        // JSON array
    private String familyMentions;  // JSON array
    private String aiInsight;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getMood() { return mood; }
    public void setMood(Integer mood) { this.mood = mood; }
    public String getEmotions() { return emotions; }
    public void setEmotions(String emotions) { this.emotions = emotions; }
    public String getFamilyMentions() { return familyMentions; }
    public void setFamilyMentions(String familyMentions) { this.familyMentions = familyMentions; }
    public String getAiInsight() { return aiInsight; }
    public void setAiInsight(String aiInsight) { this.aiInsight = aiInsight; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
