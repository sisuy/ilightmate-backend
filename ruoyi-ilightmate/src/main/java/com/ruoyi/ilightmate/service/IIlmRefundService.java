package com.ruoyi.ilightmate.service;

public interface IIlmRefundService {

    /**
     * 发起退款
     * - 更新订单状态为 REFUNDED
     * - 撤销订阅（降为体验版）
     * - 调用支付宝/微信退款 API
     *
     * @param orderNo   订单号
     * @param reason    退款原因
     * @param refundAmount 退款金额（null = 全额退款）
     */
    void refund(String orderNo, String reason, java.math.BigDecimal refundAmount);
}
