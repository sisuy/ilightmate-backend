package com.ruoyi.ilightmate.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.ruoyi.ilightmate.config.AlipayConfig;
import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.mapper.IlmOrderMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.service.IIlmRefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class IlmRefundServiceImpl implements IIlmRefundService {

    private static final Logger log = LoggerFactory.getLogger(IlmRefundServiceImpl.class);

    @Autowired
    private IlmOrderMapper orderMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    @Transactional
    public void refund(String orderNo, String reason, BigDecimal refundAmount) {
        IlmOrder order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"PAID".equals(order.getPayStatus())) {
            throw new RuntimeException("订单未支付，无法退款");
        }

        BigDecimal actualRefund = refundAmount != null ? refundAmount : order.getPayAmount();

        // 1. 调用第三方退款 API
        if ("ALIPAY".equals(order.getPayType())) {
            refundAlipay(order, actualRefund, reason);
        } else if ("WECHAT".equals(order.getPayType())) {
            refundWechat(order, actualRefund, reason);
        }

        // 2. 更新订单状态
        orderMapper.updatePayStatus(orderNo, "REFUNDED", order.getPayType(), order.getTransactionNo(), new Date());

        // 3. 撤销订阅（所有该用户的活跃订阅降为过期）
        subscriptionMapper.expireByUserId(order.getUserId());

        log.info("Refund completed: orderNo={} amount={} reason={}", orderNo, actualRefund, reason);
    }

    private void refundAlipay(IlmOrder order, BigDecimal amount, String reason) {
        try {
            AlipayClient client = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json", "UTF-8",
                    alipayConfig.getPublicKey(),
                    "RSA2"
            );

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent(String.format(
                    "{\"out_trade_no\":\"%s\",\"refund_amount\":\"%s\",\"refund_reason\":\"%s\"}",
                    order.getOrderNo(), amount.toPlainString(), reason != null ? reason : "用户申请退款"
            ));

            AlipayTradeRefundResponse response = client.execute(request);
            if (!response.isSuccess()) {
                log.error("Alipay refund failed: {} {}", response.getSubCode(), response.getSubMsg());
                throw new RuntimeException("支付宝退款失败: " + response.getSubMsg());
            }
        } catch (Exception e) {
            log.error("Alipay refund error: {}", e.getMessage());
            throw new RuntimeException("支付宝退款异常: " + e.getMessage());
        }
    }

    private void refundWechat(IlmOrder order, BigDecimal amount, String reason) {
        // TODO: 使用 wechatpay-java SDK 调用退款接口
        // RefundService refundService = new RefundService.Builder().config(config).build();
        // CreateRequest createRequest = new CreateRequest();
        // createRequest.setOutTradeNo(order.getOrderNo());
        // createRequest.setOutRefundNo("REF" + order.getOrderNo());
        // AmountReq amountReq = new AmountReq();
        // amountReq.setRefund(amount.multiply(new BigDecimal(100)).longValue());
        // amountReq.setTotal(order.getPayAmount().multiply(new BigDecimal(100)).longValue());
        // amountReq.setCurrency("CNY");
        // createRequest.setAmount(amountReq);
        // createRequest.setReason(reason);
        // Refund refund = refundService.create(createRequest);

        log.info("WeChat refund requested for order {} amount {}", order.getOrderNo(), amount);
    }
}
