package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.dto.CreateOrderDTO;
import com.ruoyi.ilightmate.mapper.IlmComboPlanMapper;
import com.ruoyi.ilightmate.mapper.IlmOrderMapper;
import com.ruoyi.ilightmate.mapper.IlmUserAttributionMapper;
import com.ruoyi.ilightmate.service.IIlmComboService;
import com.ruoyi.ilightmate.service.IIlmOrderService;
import com.ruoyi.ilightmate.service.IIlmTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class IlmOrderServiceImpl implements IIlmOrderService {

    private static final Logger log = LoggerFactory.getLogger(IlmOrderServiceImpl.class);

    /** 成长版首月特惠价 */
    private static final BigDecimal FIRST_MONTH_PRICE = new BigDecimal("9.90");

    /** 导师邀请客户付费后的 token 奖励 */
    private static final int MENTOR_TOKEN_REWARD = 200_000;

    @Autowired
    private IlmOrderMapper orderMapper;

    @Autowired
    private IlmComboPlanMapper comboPlanMapper;

    @Autowired
    private IIlmComboService comboService;

    @Autowired
    private IIlmTokenService tokenService;

    @Autowired(required = false)
    private IlmUserAttributionMapper attributionMapper;

    @Override
    public IlmOrder createOrder(Long userId, CreateOrderDTO dto) {
        IlmComboPlan plan = comboPlanMapper.selectById(dto.getComboId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        // ── 计算实际支付金额 ──
        BigDecimal payAmount = plan.getPrice();

        // 成长版月付首月特惠：检查用户是否从未购买过成长版
        if ("1".equals(plan.getComboCode()) && "MONTHLY".equals(plan.getBillingCycle())) {
            boolean isFirstPurchase = !orderMapper.hasEverPaid(userId, "1");
            if (isFirstPurchase) {
                payAmount = FIRST_MONTH_PRICE;
                log.info("User {} gets first month discount: ¥{}", userId, FIRST_MONTH_PRICE);
            }
        }

        IlmOrder order = new IlmOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setComboId(dto.getComboId());
        order.setPayAmount(payAmount);
        order.setOrderStatus("CREATED");
        order.setPayStatus("UNPAID");
        order.setReferralCode(dto.getReferralCode());
        order.setRegion(dto.getRegion() != null ? dto.getRegion() : "CN");
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);
        return order;
    }

    @Override
    public IlmOrder getById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public IlmOrder getByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<IlmOrder> listByUserId(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    /**
     * 标记订单已支付 — 触发完整的支付后流程
     *
     * 流程：
     * 1. 更新订单状态
     * 2. 激活订阅（处理新购/续费/升级）
     * 3. 首次付费锁定归因
     * 4. 导师 MT 码 token 奖励
     */
    @Override
    @Transactional
    public void markPaid(String orderNo, String payType, String transactionNo) {
        IlmOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("Order not found: {}", orderNo);
            return;
        }
        if ("PAID".equals(order.getPayStatus())) {
            log.warn("Order {} already paid, skip", orderNo);
            return;
        }

        Long userId = order.getUserId();

        // ── 1. 更新订单状态 ──
        orderMapper.updatePayStatus(orderNo, "PAID", payType, transactionNo, new Date());

        // ── 2. 区分订单类型 ──
        String remark = order.getRemark();
        boolean isTokenAddon = remark != null && remark.startsWith("TOKEN_ADDON:");
        boolean isAutoRenew = remark != null && remark.startsWith("AUTO_RENEW:");

        if (isTokenAddon) {
            // 加购包订单 → 增加 token
            String[] parts = remark.split(":");
            int tokens = Integer.parseInt(parts[2]);
            tokenService.addBonusTokens(userId, tokens);
            log.info("Token addon paid: user={} tokens=+{}", userId, tokens);
            return; // 加购包不需要激活订阅和归因
        }

        // ── 3. 激活订阅（新购 / 续费 / 升级） ──
        comboService.activateSubscription(userId, order.getComboId(), orderNo);

        // ── 3. 首次付费 → 锁定归因 ──
        if (attributionMapper != null) {
            Boolean alreadyLocked = attributionMapper.isLocked(userId);
            if (alreadyLocked == null || !alreadyLocked) {
                attributionMapper.lockAttribution(userId);
                log.info("Attribution locked for user {} after first payment", userId);

                // ── 4. 如果是导师邀请的客户，给导师 token 奖励 ──
                String entryType = attributionMapper.getEntryType(userId);
                if ("mentor_referral".equals(entryType)) {
                    Long mentorUserId = attributionMapper.getMentorUserId(userId);
                    if (mentorUserId != null) {
                        tokenService.addBonusTokens(mentorUserId, MENTOR_TOKEN_REWARD);
                        log.info("Mentor {} rewarded {}K tokens for client {} payment",
                                mentorUserId, MENTOR_TOKEN_REWARD / 1000, userId);
                    }
                }
            }
        }

        log.info("Order {} paid via {} tx={}", orderNo, payType, transactionNo);
    }

    private String generateOrderNo() {
        return "ILM" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
