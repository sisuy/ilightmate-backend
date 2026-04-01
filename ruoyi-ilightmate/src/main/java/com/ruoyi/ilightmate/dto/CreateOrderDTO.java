package com.ruoyi.ilightmate.dto;

/**
 * 创建订单 DTO — 匹配前端 src/api/payment.ts 的 createOrder
 */
public class CreateOrderDTO {

    private Long comboId;
    private String remark;
    private String region;
    private String referralCode;

    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
}
