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

    @Override
    public void activateSubscription(Long userId, Long comboId, String orderNo) {
        IlmComboPlan plan = comboPlanMapper.selectById(comboId);
        if (plan == null) {
            log.error("Cannot activate subscription: plan {} not found", comboId);
            return;
        }

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        // 根据 billingCycle 计算结束时间
        if ("YEARLY".equals(plan.getBillingCycle())) {
            cal.add(Calendar.YEAR, 1);
        } else {
            cal.add(Calendar.MONTH, 1);
        }

        IlmUserSubscription sub = new IlmUserSubscription();
        sub.setUserId(userId);
        sub.setComboId(comboId);
        sub.setComboCode(plan.getComboCode());
        sub.setComboName(plan.getComboName());
        sub.setComboType(plan.getComboType());
        sub.setDailyConsultLimit(plan.getDailyDialogueLimit());
        sub.setStartTime(now);
        sub.setEndTime(cal.getTime());

        subscriptionMapper.insert(sub);
        log.info("Activated subscription for user {} -> {} until {}", userId, plan.getComboName(), cal.getTime());
    }
}
