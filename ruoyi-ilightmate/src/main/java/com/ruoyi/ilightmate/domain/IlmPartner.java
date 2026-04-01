package com.ruoyi.ilightmate.domain;

import java.util.Date;

/**
 * 推荐合作伙伴（渠道 CH- / 直销 SL- / 导师 MT-）
 */
public class IlmPartner {
    private Long id;
    private String partnerType;     // channel / sales_rep / mentor
    private String name;
    private String phone;
    private Long userId;            // 关联 sys_user.user_id（导师类型必填）
    private String referralCode;    // CH-XXXX / SL-XXXX / MT-XXXX
    private Long parentId;          // 上线伙伴 ID
    private Long salesRepId;        // 对接业务员 ID
    private String status;          // 1=正常 0=禁用
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPartnerType() { return partnerType; }
    public void setPartnerType(String partnerType) { this.partnerType = partnerType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getSalesRepId() { return salesRepId; }
    public void setSalesRepId(Long salesRepId) { this.salesRepId = salesRepId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
