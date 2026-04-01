package com.ruoyi.ilightmate.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

/**
 * 套餐计划
 * 对应表 ilm_combo_plans
 */
public class IlmComboPlan extends BaseEntity {

    private Long comboId;
    private String comboName;
    private String comboCode;       // '0'/'1'/'2'
    private String comboType;       // FREE/GROWTH/PROFESSIONAL
    private BigDecimal price;
    private String billingCycle;    // MONTHLY/YEARLY
    private String description;
    private String benefitsJson;    // JSON string
    private int monthlyTokenLimit;
    private int dailyDialogueLimit;
    private int trialDays;
    private String region;
    private String status;
    private int orderNum;
    private String remark;

    // Getters and Setters
    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }
    public String getComboName() { return comboName; }
    public void setComboName(String comboName) { this.comboName = comboName; }
    public String getComboCode() { return comboCode; }
    public void setComboCode(String comboCode) { this.comboCode = comboCode; }
    public String getComboType() { return comboType; }
    public void setComboType(String comboType) { this.comboType = comboType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBenefitsJson() { return benefitsJson; }
    public void setBenefitsJson(String benefitsJson) { this.benefitsJson = benefitsJson; }
    public int getMonthlyTokenLimit() { return monthlyTokenLimit; }
    public void setMonthlyTokenLimit(int monthlyTokenLimit) { this.monthlyTokenLimit = monthlyTokenLimit; }
    public int getDailyDialogueLimit() { return dailyDialogueLimit; }
    public void setDailyDialogueLimit(int dailyDialogueLimit) { this.dailyDialogueLimit = dailyDialogueLimit; }
    public int getTrialDays() { return trialDays; }
    public void setTrialDays(int trialDays) { this.trialDays = trialDays; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getOrderNum() { return orderNum; }
    public void setOrderNum(int orderNum) { this.orderNum = orderNum; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
