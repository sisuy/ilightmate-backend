package com.ruoyi.ilightmate.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 月度 Token 使用量
 * 对应表 ilm_token_usage
 */
public class IlmTokenUsage extends BaseEntity {

    private Long id;
    private Long userId;
    private String monthKey;  // YYYY-MM
    private int used;
    private int bonus;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }
    public int getBonus() { return bonus; }
    public void setBonus(int bonus) { this.bonus = bonus; }
}
