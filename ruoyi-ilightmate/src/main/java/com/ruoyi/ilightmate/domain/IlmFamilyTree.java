package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmFamilyTree {
    private Long id;
    private Long userId;
    private String treeName;
    private String treeData;        // JSON string
    private Integer memberCount;
    private Integer generationCount;
    private String analysisData;    // JSON string
    private String patterns;        // JSON string
    private String familyType;
    private String direction;
    private String riskLevel;
    private Date createTime;
    private Date updateTime;

    // Generate all getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTreeName() { return treeName; }
    public void setTreeName(String treeName) { this.treeName = treeName; }
    public String getTreeData() { return treeData; }
    public void setTreeData(String treeData) { this.treeData = treeData; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public Integer getGenerationCount() { return generationCount; }
    public void setGenerationCount(Integer generationCount) { this.generationCount = generationCount; }
    public String getAnalysisData() { return analysisData; }
    public void setAnalysisData(String analysisData) { this.analysisData = analysisData; }
    public String getPatterns() { return patterns; }
    public void setPatterns(String patterns) { this.patterns = patterns; }
    public String getFamilyType() { return familyType; }
    public void setFamilyType(String familyType) { this.familyType = familyType; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
