package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmOrderMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.service.IIlmTokenAddonService;
import com.ruoyi.ilightmate.service.IIlmTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Token 加购包服务
 *
 * 加购包定义（匹配前端 plan-benefits.ts 的 TOKEN_ADDONS）：
 * - addon-growth-100k:  成长版用户，¥9.9 / 100K tokens
 * - addon-pro-500k:     专业版用户，¥49 / 500K tokens
 * - addon-pro-5m:       专业版用户，¥399 / 5M tokens
 */
@Service
public class IlmTokenAddonServiceImpl implements IIlmTokenAddonService {

    private static final Logger log = LoggerFactory.getLogger(IlmTokenAddonServiceImpl.class);

    /** 加购包配置 */
    private static final Map<String, AddonDef> ADDONS = new LinkedHashMap<>();
    static {
        ADDONS.put("addon-growth-100k", new AddonDef("addon-growth-100k", "成长加油包", 100_000, new BigDecimal("9.90"), "1"));
        ADDONS.put("addon-pro-500k",    new AddonDef("addon-pro-500k",    "专业补充包", 500_000, new BigDecimal("49.00"), "2"));
        ADDONS.put("addon-pro-5m",      new AddonDef("addon-pro-5m",      "专业流量包", 5_000_000, new BigDecimal("399.00"), "2"));
    }

    @Autowired
    private IlmOrderMapper orderMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Autowired
    private IIlmTokenService tokenService;

    @Override
    public List<Map<String, Object>> listAvailableAddons(Long userId) {
        IlmUserSubscription sub = subscriptionMapper.selectActiveByUserId(userId);
        String userCode = sub != null ? sub.getComboCode() : "0";
        int userLevel = codeToLevel(userCode);

        List<Map<String, Object>> result = new ArrayList<>();
        for (AddonDef addon : ADDONS.values()) {
            int minLevel = codeToLevel(addon.minPlan);
            if (userLevel >= minLevel) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", addon.id);
                item.put("name", addon.name);
                item.put("tokens", addon.tokens);
                item.put("price", addon.price);
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public IlmOrder createAddonOrder(Long userId, String addonId) {
        AddonDef addon = ADDONS.get(addonId);
        if (addon == null) {
            throw new RuntimeException("加购包不存在: " + addonId);
        }

        // 检查用户套餐等级
        IlmUserSubscription sub = subscriptionMapper.selectActiveByUserId(userId);
        String userCode = sub != null ? sub.getComboCode() : "0";
        if (codeToLevel(userCode) < codeToLevel(addon.minPlan)) {
            throw new RuntimeException("当前套餐不支持此加购包，请先升级");
        }

        IlmOrder order = new IlmOrder();
        order.setOrderNo("ILMA" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000)));
        order.setUserId(userId);
        order.setComboId(-1L); // 加购包没有 combo_id，用 -1 标记
        order.setPayAmount(addon.price);
        order.setOrderStatus("CREATED");
        order.setPayStatus("UNPAID");
        order.setRemark("TOKEN_ADDON:" + addonId + ":" + addon.tokens);

        orderMapper.insert(order);
        return order;
    }

    @Override
    public void confirmAddonPayment(String orderNo) {
        IlmOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !"UNPAID".equals(order.getPayStatus())) {
            return;
        }

        // 解析 remark 获取 token 数量
        String remark = order.getRemark();
        if (remark == null || !remark.startsWith("TOKEN_ADDON:")) {
            log.error("Invalid addon order remark: {}", remark);
            return;
        }

        String[] parts = remark.split(":");
        int tokens = Integer.parseInt(parts[2]);

        // 加 token
        tokenService.addBonusTokens(order.getUserId(), tokens);

        // 更新订单状态
        orderMapper.updatePayStatus(orderNo, "PAID", null, null, new Date());

        log.info("Token addon confirmed: user={} tokens=+{} orderNo={}", order.getUserId(), tokens, orderNo);
    }

    private int codeToLevel(String code) {
        if ("1".equals(code)) return 1;
        if ("2".equals(code)) return 2;
        return 0;
    }

    /** 加购包定义 */
    private static class AddonDef {
        final String id;
        final String name;
        final int tokens;
        final BigDecimal price;
        final String minPlan;

        AddonDef(String id, String name, int tokens, BigDecimal price, String minPlan) {
            this.id = id;
            this.name = name;
            this.tokens = tokens;
            this.price = price;
            this.minPlan = minPlan;
        }
    }
}
