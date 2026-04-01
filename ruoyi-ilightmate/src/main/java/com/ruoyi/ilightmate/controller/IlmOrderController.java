package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.dto.CreateOrderDTO;
import com.ruoyi.ilightmate.domain.IlmOrder;
import com.ruoyi.ilightmate.service.IIlmOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 Controller
 *
 * 匹配前端 src/api/payment.ts 的 createOrder
 */
@RestController
@RequestMapping("/api/comboOrder")
public class IlmOrderController {

    @Autowired
    private IIlmOrderService orderService;

    /**
     * 创建订单
     * POST /api/comboOrder/create-minimal
     */
    @PostMapping("/create-minimal")
    public AjaxResult createOrder(@RequestBody CreateOrderDTO dto) {
        Long userId = SecurityUtils.getUserId();
        IlmOrder order = orderService.createOrder(userId, dto);
        return AjaxResult.success(order);
    }

    /**
     * 查询用户订单
     * GET /api/comboOrder/query?userId=X
     */
    @GetMapping("/query")
    public AjaxResult queryOrders(@RequestParam Long userId) {
        return AjaxResult.success(orderService.listByUserId(userId));
    }

    /**
     * 查询单个订单
     * GET /api/comboOrder/{orderId}
     */
    @GetMapping("/{orderId}")
    public AjaxResult getOrder(@PathVariable Long orderId) {
        return AjaxResult.success(orderService.getById(orderId));
    }
}
