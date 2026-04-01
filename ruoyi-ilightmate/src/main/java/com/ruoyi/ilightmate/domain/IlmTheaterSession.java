package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmTheaterSession {
    private Long id;
    private Long userId;
    private String treeId;
    private String realityPositions;    // JSON
    private String idealPositions;      // JSON
    private String patterns;            // JSON
    private String coreInsight;
    private String actionCommitment;
    private String radarBefore;         // JSON
    private String radarAfter;          // JSON
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTreeId() { return treeId; }
    public void setTreeId(String treeId) { this.treeId = treeId; }
    public String getRealityPositions() { return realityPositions; }
    public void setRealityPositions(String realityPositions) { this.realityPositions = realityPositions; }
    public String getIdealPositions() { return idealPositions; }
    public void setIdealPositions(String idealPositions) { this.idealPositions = idealPositions; }
    public String getPatterns() { return patterns; }
    public void setPatterns(String patterns) { this.patterns = patterns; }
    public String getCoreInsight() { return coreInsight; }
    public void setCoreInsight(String coreInsight) { this.coreInsight = coreInsight; }
    public String getActionCommitment() { return actionCommitment; }
    public void setActionCommitment(String actionCommitment) { this.actionCommitment = actionCommitment; }
    public String getRadarBefore() { return radarBefore; }
    public void setRadarBefore(String radarBefore) { this.radarBefore = radarBefore; }
    public String getRadarAfter() { return radarAfter; }
    public void setRadarAfter(String radarAfter) { this.radarAfter = radarAfter; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
