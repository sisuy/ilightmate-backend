package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;

import java.util.List;

public interface IIlmComboService {

    /** 获取用户当前有效订阅 */
    IlmUserSubscription getActiveSubscription(Long userId);

    /** 获取所有活跃套餐 */
    List<IlmComboPlan> listActivePlans();

    /** 根据 ID 获取套餐 */
    IlmComboPlan getPlanById(Long comboId);

    /** 支付成功后激活订阅 */
    void activateSubscription(Long userId, Long comboId, String orderNo);
}
