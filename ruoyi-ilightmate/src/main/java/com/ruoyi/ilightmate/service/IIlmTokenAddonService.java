package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmOrder;

import java.util.List;
import java.util.Map;

public interface IIlmTokenAddonService {

    /** 获取用户可购买的加购包（根据当前套餐过滤） */
    List<Map<String, Object>> listAvailableAddons(Long userId);

    /** 创建加购包订单 */
    IlmOrder createAddonOrder(Long userId, String addonId);

    /** 加购包支付确认 → 增加 token */
    void confirmAddonPayment(String orderNo);
}
