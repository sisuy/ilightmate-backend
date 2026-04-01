package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmComboPlanMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.service.IIlmComboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class IlmComboServiceImpl implements IIlmComboService {

    private static final Logger log = LoggerFactory.getLogger(IlmComboServiceImpl.class);

    @Autowired
    private IlmComboPlanMapper comboPlanMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Override
    public IlmUserSubscription getActiveSubscription(Long userId) {
        return subscriptionMapper.selectActiveByUserId(userId);
    }

    @Override
    public List<IlmComboPlan> listActivePlans() {
        return comboPlanMapper.selectActivePlans();
    }

    @Override
    public IlmComboPlan getPlanById(Long comboId) {
        return comboPlanMapper.selectById(comboId);
    }

    /**
     * 激活订阅 — 核心业务逻辑
     *
     * 处理场景：
     * 1. 新用户首次购买
     * 2. 试用期转正式
     * 3. 月付续费（在现有订阅上延长）
     * 4. 年付续费
     * 5. 升级（成长版 → 专业版）
     * 6. 降级不支持（只能等到期）
     */
    @Override
    @Transactional
    public void activateSubscription(Long userId, Long comboId, String orderNo) {
        IlmComboPlan plan = comboPlanMapper.selectById(comboId);
        if (plan == null) {
            log.error("Cannot activate subscription: plan {} not found", comboId);
            return;
        }

        // 查找当前有效订阅
        IlmUserSubscription currentSub = subscriptionMapper.selectActiveByUserId(userId);

        Date startTime;
        Date endTime;

        if (currentSub != null) {
            // ── 已有订阅的情况 ──

            int currentLevel = codeToLevel(currentSub.getComboCode());
            int newLevel = codeToLevel(plan.getComboCode());

            if (newLevel > currentLevel) {
                // 升级：立即生效，旧订阅作废
                subscriptionMapper.expireByUserId(userId);
                startTime = new Date();
                endTime = calculateEndTime(startTime, plan);
                log.info("User {} upgraded from {} to {}", userId, currentSub.getComboName(), plan.getComboName());

            } else if (newLevel == currentLevel) {
                // 同级续费：在现有到期时间上延长
                startTime = currentSub.getStartTime();
                Date baseTime = currentSub.getEndTime().after(new Date()) ? currentSub.getEndTime() : new Date();
                endTime = calculateEndTime(baseTime, plan);
                subscriptionMapper.expireByUserId(userId);
                log.info("User {} renewed {} until {}", userId, plan.getComboName(), endTime);

            } else {
                // 降级：不允许。用户只能等到期后购买低级套餐。
                log.warn("User {} attempted downgrade from {} to {}, rejected", userId, currentSub.getComboName(), plan.getComboName());
                return;
            }
        } else {
            // ── 没有订阅（新用户或已过期）──
            startTime = new Date();
            endTime = calculateEndTime(startTime, plan);
        }

        // 创建新订阅记录
        IlmUserSubscription sub = new IlmUserSubscription();
        sub.setUserId(userId);
        sub.setComboId(comboId);
        sub.setComboCode(plan.getComboCode());
        sub.setComboName(plan.getComboName());
        sub.setComboType(plan.getComboType());
        sub.setDailyConsultLimit(plan.getDailyDialogueLimit());
        sub.setStartTime(startTime);
        sub.setEndTime(endTime);

        subscriptionMapper.insert(sub);
        log.info("Subscription activated: user={} plan={} start={} end={}", userId, plan.getComboName(), startTime, endTime);
    }

    /**
     * 激活试用 — 不创建订单，直接给权益
     *
     * 场景：
     * - 成长版 7 天试用（首周注册送）
     * - 专业版 10 天试用
     * - 邀请奖励 7 天试用
     */
    @Override
    @Transactional
    public void activateTrial(Long userId, String comboCode, int trialDays) {
        // 检查是否已有活跃订阅（有就不给试用）
        IlmUserSubscription currentSub = subscriptionMapper.selectActiveByUserId(userId);
        if (currentSub != null && codeToLevel(currentSub.getComboCode()) >= codeToLevel(comboCode)) {
            log.info("User {} already has {} subscription, skip trial", userId, currentSub.getComboName());
            return;
        }

        // 查找对应套餐（取月付版本）
        IlmComboPlan plan = comboPlanMapper.selectByCodeAndCycle(comboCode, "MONTHLY");
        if (plan == null) {
            plan = comboPlanMapper.selectByCodeAndCycle(comboCode, "YEARLY");
        }
        if (plan == null) {
            log.error("Trial plan not found for code {}", comboCode);
            return;
        }

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, trialDays);

        // 如果有旧的低级订阅，先过期
        if (currentSub != null) {
            subscriptionMapper.expireByUserId(userId);
        }

        IlmUserSubscription sub = new IlmUserSubscription();
        sub.setUserId(userId);
        sub.setComboId(plan.getComboId());
        sub.setComboCode(plan.getComboCode());
        sub.setComboName(plan.getComboName() + "（试用）");
        sub.setComboType(plan.getComboType());
        sub.setDailyConsultLimit(plan.getDailyDialogueLimit());
        sub.setStartTime(now);
        sub.setEndTime(cal.getTime());
        sub.setStatus("3"); // 3 = trial

        subscriptionMapper.insert(sub);
        log.info("Trial activated: user={} plan={} days={} until={}", userId, plan.getComboName(), trialDays, cal.getTime());
    }

    // ── 内部方法 ──

    /** 套餐级别：0 < 1 < 2 */
    private int codeToLevel(String code) {
        if (code == null) return 0;
        switch (code) {
            case "0": return 0;
            case "1": return 1;
            case "2": return 2;
            default: return 0;
        }
    }

    /** 根据 billingCycle 计算结束时间 */
    private Date calculateEndTime(Date baseTime, IlmComboPlan plan) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseTime);

        if ("YEARLY".equals(plan.getBillingCycle())) {
            cal.add(Calendar.YEAR, 1);
        } else {
            cal.add(Calendar.MONTH, 1);
        }

        return cal.getTime();
    }
}
