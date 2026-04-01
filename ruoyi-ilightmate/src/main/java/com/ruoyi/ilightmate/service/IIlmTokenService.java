package com.ruoyi.ilightmate.service;

/**
 * Token 计量服务接口
 *
 * 服务端权威计量，Redis 热路径 + MySQL 持久化
 */
public interface IIlmTokenService {

    /**
     * 检查用户本月 token 是否已超额
     */
    boolean isMonthlyLimitReached(Long userId);

    /**
     * 检查用户今日对话次数是否已超限
     */
    boolean isDailyDialogueLimitReached(Long userId);

    /**
     * 增加 token 使用量
     */
    void incrementTokenUsage(Long userId, int tokens);

    /**
     * 增加每日对话计数
     */
    void incrementDialogueCount(Long userId);

    /**
     * 获取用户本月已用 token 数
     */
    int getMonthlyUsed(Long userId);

    /**
     * 获取用户本月 token 上限（基于套餐）
     */
    int getMonthlyLimit(Long userId);

    /**
     * 添加加购 token
     */
    void addBonusTokens(Long userId, int tokens);
}
