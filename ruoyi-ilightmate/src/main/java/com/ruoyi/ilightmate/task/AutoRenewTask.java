package com.ruoyi.ilightmate.task;

import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmActivityMapper;
import com.ruoyi.ilightmate.mapper.IlmOrderMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.service.IIlmComboService;
import com.ruoyi.ilightmate.service.IIlmPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 月付自动续费定时任务
 *
 * 配置：
 * - 调用目标：ilmAutoRenewTask.execute()
 * - Cron：0 30 8 * * ?（每天 08:30）
 *
 * 逻辑：
 * 1. 查找今天到期的月付订阅（且用户已签约代扣）
 * 2. 创建续费订单
 * 3. 调用支付宝/微信代扣接口
 * 4. 扣款成功 → 自动续费
 * 5. 扣款失败 → 发提醒，3 天后再试一次
 *
 * 注意：代扣需要用户先签约（支付宝周期扣款 / 微信委托代扣）。
 * 签约在首次购买月付时完成，签约协议号存在 ilm_orders.remark 中。
 */
@Component("ilmAutoRenewTask")
public class AutoRenewTask {

    private static final Logger log = LoggerFactory.getLogger(AutoRenewTask.class);

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Autowired
    private IlmOrderMapper orderMapper;

    @Autowired
    private IIlmComboService comboService;

    @Autowired
    private IlmActivityMapper activityMapper;

    public void execute() {
        // 查找今天到期的月付订阅（billing_cycle = MONTHLY）
        List<IlmUserSubscription> expiring = subscriptionMapper.selectMonthlyExpiringSoon(0);

        int success = 0;
        int failed = 0;

        for (IlmUserSubscription sub : expiring) {
            try {
                // 检查用户是否有代扣签约
                String agreementNo = getAutoRenewAgreement(sub.getUserId());
                if (agreementNo == null) {
                    // 没有签约代扣，跳过（用户需要手动续费）
                    log.debug("User {} has no auto-renew agreement, skip", sub.getUserId());
                    continue;
                }

                // 创建续费订单
                IlmOrder renewOrder = new IlmOrder();
                renewOrder.setOrderNo("ILMR" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000)));
                renewOrder.setUserId(sub.getUserId());
                renewOrder.setComboId(sub.getComboId());
                // 续费按原价（不享受首月特惠）
                renewOrder.setPayAmount(getMonthlyPrice(sub.getComboCode()));
                renewOrder.setOrderStatus("CREATED");
                renewOrder.setPayStatus("UNPAID");
                renewOrder.setRemark("AUTO_RENEW:" + agreementNo);
                orderMapper.insert(renewOrder);

                // 调用代扣
                boolean deducted = executeAutoDeduct(agreementNo, renewOrder);

                if (deducted) {
                    // 扣款成功 → 激活续费
                    comboService.activateSubscription(sub.getUserId(), sub.getComboId(), renewOrder.getOrderNo());
                    orderMapper.updatePayStatus(renewOrder.getOrderNo(), "PAID", "AUTO_DEDUCT", agreementNo, new java.util.Date());
                    success++;
                    log.info("Auto-renew success: user={} plan={}", sub.getUserId(), sub.getComboName());
                } else {
                    failed++;
                    log.warn("Auto-renew deduct failed: user={}", sub.getUserId());
                    try {
                        activityMapper.insert(sub.getUserId(), "system", "续费失败提醒",
                                "自动续费未成功，请手动续费以继续使用" + sub.getComboName());
                    } catch (Exception e) {
                        log.warn("Failed to save renew-failure remind for user {}: {}", sub.getUserId(), e.getMessage());
                    }
                }

            } catch (Exception e) {
                failed++;
                log.error("Auto-renew error for user {}: {}", sub.getUserId(), e.getMessage());
            }
        }

        log.info("[AutoRenew] Total={} Success={} Failed={}", expiring.size(), success, failed);
    }

    /**
     * 获取用户的代扣签约协议号
     * 从用户最近的已支付月付订单的 remark 中提取
     */
    private String getAutoRenewAgreement(Long userId) {
        // 查找用户最近的月付订单，看是否有签约协议
        IlmOrder lastOrder = orderMapper.selectLastPaidMonthlyByUserId(userId);
        if (lastOrder == null || lastOrder.getRemark() == null) return null;

        // 协议号格式：AGREEMENT:xxxxxx
        String remark = lastOrder.getRemark();
        if (remark.startsWith("AGREEMENT:")) {
            return remark.substring("AGREEMENT:".length());
        }
        return null;
    }

    /**
     * 执行代扣
     * 支付宝：alipay.trade.pay 接口 + agreement_no
     * 微信：contract_id 委托代扣
     */
    private boolean executeAutoDeduct(String agreementNo, IlmOrder order) {
        // Stub: payment SDK not yet integrated (Alipay / WeChat auto-deduct)
        // Alipay docs: https://opendocs.alipay.com/open/20190319
        // WeChat docs: https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter6_1_1.shtml

        log.info("Auto-deduct attempt: agreement={} orderNo={} amount={}",
                agreementNo, order.getOrderNo(), order.getPayAmount());

        // Returns false until payment SDK is wired up
        return false;
    }

    private java.math.BigDecimal getMonthlyPrice(String comboCode) {
        switch (comboCode) {
            case "1": return new java.math.BigDecimal("19.00"); // 成长版月价
            default: return new java.math.BigDecimal("0.00");
        }
    }
}
