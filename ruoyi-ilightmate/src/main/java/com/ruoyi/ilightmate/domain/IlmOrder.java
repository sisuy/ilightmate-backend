package com.ruoyi.ilightmate.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单
 * 对应表 ilm_orders
 */
public class IlmOrder extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long comboId;
    private BigDecimal payAmount;
    private String orderStatus;   // CREATED/PAID/CANCELLED/EXPIRED
    private String payStatus;     // UNPAID/PAID/REFUNDED
    private String payType;       // ALIPAY/WECHAT
    private Date payTime;
    private Date startTime;
    private Date endTime;
    private String transactionNo;
    private String referralCode;
    private String region;
    private String remark;

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getTransactionNo() { return transactionNo; }
    public void setTransactionNo(String transactionNo) { this.transactionNo = transactionNo; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
