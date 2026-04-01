package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.dto.CreateOrderDTO;
import com.ruoyi.ilightmate.mapper.IlmComboPlanMapper;
import com.ruoyi.ilightmate.mapper.IlmOrderMapper;
import com.ruoyi.ilightmate.service.IIlmComboService;
import com.ruoyi.ilightmate.service.IIlmOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class IlmOrderServiceImpl implements IIlmOrderService {

    private static final Logger log = LoggerFactory.getLogger(IlmOrderServiceImpl.class);

    @Autowired
    private IlmOrderMapper orderMapper;

    @Autowired
    private IlmComboPlanMapper comboPlanMapper;

    @Autowired
    private IIlmComboService comboService;

    @Override
    public IlmOrder createOrder(Long userId, CreateOrderDTO dto) {
        IlmComboPlan plan = comboPlanMapper.selectById(dto.getComboId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        IlmOrder order = new IlmOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setComboId(dto.getComboId());
        order.setPayAmount(plan.getPrice());
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

        // 更新订单状态
        orderMapper.updatePayStatus(orderNo, "PAID", payType, transactionNo, new Date());

        // 激活订阅
        comboService.activateSubscription(order.getUserId(), order.getComboId(), orderNo);

        log.info("Order {} paid via {} tx={}", orderNo, payType, transactionNo);
    }

    private String generateOrderNo() {
        // 格式：ILM + 时间戳 + 4 位随机
        return "ILM" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
