package com.ruoyi.ilightmate.domain;

import java.util.Date;

/**
 * 用户归因追踪
 */
public class IlmUserAttribution {
    private Long id;
    private Long userId;
    private String entryType;       // channel / direct_sales / natural / mentor_referral
    private String referralCode;
    private String referrerName;
    private Long tier1PartnerId;
    private Long tier2PartnerId;
    private Long salesRepId;
    private Boolean locked;
    private Date lockedAt;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public String getReferrerName() { return referrerName; }
    public void setReferrerName(String referrerName) { this.referrerName = referrerName; }
    public Long getTier1PartnerId() { return tier1PartnerId; }
    public void setTier1PartnerId(Long tier1PartnerId) { this.tier1PartnerId = tier1PartnerId; }
    public Long getTier2PartnerId() { return tier2PartnerId; }
    public void setTier2PartnerId(Long tier2PartnerId) { this.tier2PartnerId = tier2PartnerId; }
    public Long getSalesRepId() { return salesRepId; }
    public void setSalesRepId(Long salesRepId) { this.salesRepId = salesRepId; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
    public Date getLockedAt() { return lockedAt; }
    public void setLockedAt(Date lockedAt) { this.lockedAt = lockedAt; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
