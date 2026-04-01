package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmCoachSession {
    private Long id;
    private Long userId;
    private String theme;
    private String subTopic;
    private Integer currentStep;
    private String subStep;
    private String analysisData;            // JSON
    private String insightData;             // JSON
    private String selectedCompanion;
    private String companionDialogueData;   // JSON
    private String actionCommitment;
    private String growthCycle;             // JSON
    private String radarBefore;            // JSON
    private Boolean completed;
    private Date startedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getSubTopic() { return subTopic; }
    public void setSubTopic(String subTopic) { this.subTopic = subTopic; }
    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
    public String getSubStep() { return subStep; }
    public void setSubStep(String subStep) { this.subStep = subStep; }
    public String getAnalysisData() { return analysisData; }
    public void setAnalysisData(String analysisData) { this.analysisData = analysisData; }
    public String getInsightData() { return insightData; }
    public void setInsightData(String insightData) { this.insightData = insightData; }
    public String getSelectedCompanion() { return selectedCompanion; }
    public void setSelectedCompanion(String selectedCompanion) { this.selectedCompanion = selectedCompanion; }
    public String getCompanionDialogueData() { return companionDialogueData; }
    public void setCompanionDialogueData(String companionDialogueData) { this.companionDialogueData = companionDialogueData; }
    public String getActionCommitment() { return actionCommitment; }
    public void setActionCommitment(String actionCommitment) { this.actionCommitment = actionCommitment; }
    public String getGrowthCycle() { return growthCycle; }
    public void setGrowthCycle(String growthCycle) { this.growthCycle = growthCycle; }
    public String getRadarBefore() { return radarBefore; }
    public void setRadarBefore(String radarBefore) { this.radarBefore = radarBefore; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }
}
