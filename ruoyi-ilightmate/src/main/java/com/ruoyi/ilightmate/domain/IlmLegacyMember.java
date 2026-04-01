package com.ruoyi.ilightmate.domain;

import java.util.Date;

public class IlmLegacyMember {
    private Long id;
    private Long userId;
    private String name;
    private String role;
    private String toneStyle;
    private String atmosphere;
    private String catchphrases;    // JSON array
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToneStyle() { return toneStyle; }
    public void setToneStyle(String toneStyle) { this.toneStyle = toneStyle; }
    public String getAtmosphere() { return atmosphere; }
    public void setAtmosphere(String atmosphere) { this.atmosphere = atmosphere; }
    public String getCatchphrases() { return catchphrases; }
    public void setCatchphrases(String catchphrases) { this.catchphrases = catchphrases; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
