package com.ruoyi.ilightmate.task;

import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订阅过期定时任务
 *
 * 在 RuoYi 后台「定时任务」中配置：
 * - 调用目标：ilmSubscriptionExpireTask.execute()
 * - Cron 表达式：0 5 0 * * ?（每天 00:05 执行）
 *
 * 功能：
 * 1. 将 end_time < NOW() 且 status='1' 的订阅标记为过期（status='0'）
 * 2. 将 end_time < NOW() 且 status='3'（试用）的订阅标记为过期
 * 3. 记录过期用户数量
 */
@Component("ilmSubscriptionExpireTask")
public class SubscriptionExpireTask {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpireTask.class);

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    public void execute() {
        int expired = subscriptionMapper.expireOverdue();
        if (expired > 0) {
            log.info("[SubscriptionExpireTask] Expired {} subscriptions", expired);
        }
    }
}
