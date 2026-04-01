package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;

import java.util.List;

public interface IIlmComboService {

    /** 获取用户当前有效订阅（含试用） */
    IlmUserSubscription getActiveSubscription(Long userId);

    /** 获取所有活跃套餐 */
    List<IlmComboPlan> listActivePlans();

    /** 根据 ID 获取套餐 */
    IlmComboPlan getPlanById(Long comboId);

    /**
     * 支付成功后激活订阅
     * 处理：新购 / 续费 / 升级 / 试用转正
     */
    void activateSubscription(Long userId, Long comboId, String orderNo);

    /**
     * 激活试用（不创建订单）
     * @param comboCode 套餐代码（'1'=成长版, '2'=专业版）
     * @param trialDays 试用天数
     */
    void activateTrial(Long userId, String comboCode, int trialDays);
}
