package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.dto.CreateOrderDTO;

import java.util.List;

public interface IIlmOrderService {

    IlmOrder createOrder(Long userId, CreateOrderDTO dto);

    IlmOrder getById(Long orderId);

    IlmOrder getByOrderNo(String orderNo);

    List<IlmOrder> listByUserId(Long userId);

    /** 标记订单已支付 + 激活订阅 */
    void markPaid(String orderNo, String payType, String transactionNo);
}
