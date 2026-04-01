package com.ruoyi.ilightmate.task;

import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmActivityMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 续费提醒定时任务
 *
 * 配置：
 * - 调用目标：ilmSubscriptionRenewRemindTask.execute()
 * - Cron：0 0 10 * * ?（每天 10:00）
 *
 * 功能：
 * 1. 找到 3 天内到期的订阅用户
 * 2. 找到今天到期的订阅用户
 * 3. 找到已过期 1 天的用户（最后挽回）
 * 4. 调用推送服务发送提醒（预留接口）
 */
@Component("ilmSubscriptionRenewRemindTask")
public class SubscriptionRenewRemindTask {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRenewRemindTask.class);

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Autowired
    private IlmActivityMapper activityMapper;

    public void execute() {
        // 3 天内到期
        List<IlmUserSubscription> expiringIn3Days = subscriptionMapper.selectExpiringSoon(3);
        for (IlmUserSubscription sub : expiringIn3Days) {
            sendRemind(sub, "您的" + sub.getComboName() + "将在 3 天内到期，续费可继续享受完整功能");
        }

        // 今天到期
        List<IlmUserSubscription> expiringToday = subscriptionMapper.selectExpiringSoon(0);
        for (IlmUserSubscription sub : expiringToday) {
            sendRemind(sub, "您的" + sub.getComboName() + "今天到期，立即续费避免功能降级");
        }

        // 已过期 1 天（最后挽回）
        List<IlmUserSubscription> expiredYesterday = subscriptionMapper.selectRecentlyExpired(1);
        for (IlmUserSubscription sub : expiredYesterday) {
            sendRemind(sub, "您的" + sub.getComboName() + "已到期，续费可立即恢复所有功能");
        }

        log.info("[RenewRemind] 3d={} today={} expired={}",
                expiringIn3Days.size(), expiringToday.size(), expiredYesterday.size());
    }

    private void sendRemind(IlmUserSubscription sub, String message) {
        // Write to ilm_activities as in-app notification; user sees it in Journey activity timeline
        try {
            activityMapper.insert(sub.getUserId(), "system", "续费提醒", message);
        } catch (Exception e) {
            log.warn("Failed to save remind for user {}: {}", sub.getUserId(), e.getMessage());
        }
        log.info("[RenewRemind] user={} plan={} msg={}", sub.getUserId(), sub.getComboName(), message);
    }
}
