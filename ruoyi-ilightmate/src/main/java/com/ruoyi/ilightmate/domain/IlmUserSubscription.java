package com.ruoyi.ilightmate.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 用户订阅
 * 对应表 ilm_user_subscriptions
 */
public class IlmUserSubscription extends BaseEntity {

    private Long id;
    private Long userId;
    private Long comboId;
    private String comboCode;
    private String comboName;
    private String comboType;
    private int dailyConsultLimit;
    private Date startTime;
    private Date endTime;
    private String status;  // 1=active, 0=expired, 2=cancelled

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }
    public String getComboCode() { return comboCode; }
    public void setComboCode(String comboCode) { this.comboCode = comboCode; }
    public String getComboName() { return comboName; }
    public void setComboName(String comboName) { this.comboName = comboName; }
    public String getComboType() { return comboType; }
    public void setComboType(String comboType) { this.comboType = comboType; }
    public int getDailyConsultLimit() { return dailyConsultLimit; }
    public void setDailyConsultLimit(int dailyConsultLimit) { this.dailyConsultLimit = dailyConsultLimit; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
