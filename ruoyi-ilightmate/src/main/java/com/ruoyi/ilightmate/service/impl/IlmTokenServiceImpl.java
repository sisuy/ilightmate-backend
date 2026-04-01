package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.ilightmate.domain.IlmTokenUsage;
import com.ruoyi.ilightmate.mapper.IlmTokenUsageMapper;
import com.ruoyi.ilightmate.mapper.IlmComboPlanMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.mapper.IlmDialogueUsageMapper;
import com.ruoyi.ilightmate.service.IIlmTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Token 计量服务实现
 *
 * 热路径：Redis INCRBY（毫秒级）
 * 持久化：异步写入 MySQL（最终一致）
 * 月度重置：通过 Redis key 中的 YYYY-MM 自动隔离
 */
@Service
public class IlmTokenServiceImpl implements IIlmTokenService {

    private static final Logger log = LoggerFactory.getLogger(IlmTokenServiceImpl.class);

    private static final String TOKEN_KEY_PREFIX = "ilm:token:";
    private static final String DIALOGUE_KEY_PREFIX = "ilm:dialogue:";
    private static final int DEFAULT_TOKEN_LIMIT = 50000;  // 体验版默认
    private static final int DEFAULT_DIALOGUE_LIMIT = 5;   // 体验版默认

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IlmTokenUsageMapper tokenUsageMapper;

    @Autowired
    private IlmDialogueUsageMapper dialogueUsageMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Autowired
    private IlmComboPlanMapper comboPlanMapper;

    @Override
    public boolean isMonthlyLimitReached(Long userId) {
        int used = getMonthlyUsed(userId);
        int limit = getMonthlyLimit(userId);
        return used >= limit;
    }

    @Override
    public boolean isDailyDialogueLimitReached(Long userId) {
        int dailyLimit = getDailyDialogueLimit(userId);
        if (dailyLimit == -1) return false; // 无限

        String key = DIALOGUE_KEY_PREFIX + userId + ":" + today();
        Integer count = redisCache.getCacheObject(key);
        return count != null && count >= dailyLimit;
    }

    @Override
    public void incrementTokenUsage(Long userId, int tokens) {
        String monthKey = currentMonth();
        String redisKey = TOKEN_KEY_PREFIX + userId + ":" + monthKey;

        // Redis 热路径
        Long newVal = redisCache.redisTemplate.opsForValue().increment(redisKey, tokens);
        if (newVal != null && newVal == tokens) {
            // 首次本月使用，设置 35 天过期（覆盖月末到下月初）
            redisCache.expire(redisKey, 35, TimeUnit.DAYS);
        }

        // 异步持久化到 MySQL
        asyncPersistTokenUsage(userId, monthKey, tokens);
    }

    @Override
    public void incrementDialogueCount(Long userId) {
        String dateKey = today();
        String redisKey = DIALOGUE_KEY_PREFIX + userId + ":" + dateKey;

        Long newVal = redisCache.redisTemplate.opsForValue().increment(redisKey, 1);
        if (newVal != null && newVal == 1) {
            // 首次今日对话，设置 48 小时过期
            redisCache.expire(redisKey, 48, TimeUnit.HOURS);
        }

        // 异步持久化
        asyncPersistDialogueUsage(userId, dateKey);
    }

    @Override
    public int getMonthlyUsed(Long userId) {
        String redisKey = TOKEN_KEY_PREFIX + userId + ":" + currentMonth();
        Integer val = redisCache.getCacheObject(redisKey);
        if (val != null) return val;

        // Redis miss → 从 MySQL 加载
        IlmTokenUsage usage = tokenUsageMapper.selectByUserAndMonth(userId, currentMonth());
        if (usage != null) {
            redisCache.setCacheObject(redisKey, usage.getUsed(), 35, TimeUnit.DAYS);
            return usage.getUsed();
        }
        return 0;
    }

    @Override
    public int getMonthlyLimit(Long userId) {
        // 从用户当前订阅获取 token 上限
        Integer limit = subscriptionMapper.selectTokenLimitByUserId(userId);
        return limit != null ? limit : DEFAULT_TOKEN_LIMIT;
    }

    @Override
    public void addBonusTokens(Long userId, int tokens) {
        String monthKey = currentMonth();
        // 加购 token 等效于增加上限（在 MySQL 中记录 bonus）
        tokenUsageMapper.addBonus(userId, monthKey, tokens);
        log.info("User {} added {} bonus tokens for {}", userId, tokens, monthKey);
    }

    // ── 内部方法 ──

    private int getDailyDialogueLimit(Long userId) {
        Integer limit = subscriptionMapper.selectDialogueLimitByUserId(userId);
        return limit != null ? limit : DEFAULT_DIALOGUE_LIMIT;
    }

    private String currentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Async
    protected void asyncPersistTokenUsage(Long userId, String monthKey, int increment) {
        try {
            tokenUsageMapper.upsertUsage(userId, monthKey, increment);
        } catch (Exception e) {
            log.warn("Failed to persist token usage for user {}: {}", userId, e.getMessage());
        }
    }

    @Async
    protected void asyncPersistDialogueUsage(Long userId, String dateKey) {
        try {
            dialogueUsageMapper.upsertCount(userId, dateKey);
        } catch (Exception e) {
            log.warn("Failed to persist dialogue usage for user {}: {}", userId, e.getMessage());
        }
    }
}
