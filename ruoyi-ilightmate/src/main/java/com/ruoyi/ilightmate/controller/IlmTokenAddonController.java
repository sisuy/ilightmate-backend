package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.service.IIlmTokenAddonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Token 加购包 Controller
 *
 * 前端 CheckoutDialog 中 mode='token-addon' 时调用
 */
@RestController
@RequestMapping("/api/tokenAddon")
public class IlmTokenAddonController {

    @Autowired
    private IIlmTokenAddonService addonService;

    /**
     * 获取可用加购包列表
     */
    @GetMapping("/list")
    public AjaxResult listAddons() {
        return AjaxResult.success(addonService.listAvailableAddons(SecurityUtils.getUserId()));
    }

    /**
     * 创建加购包订单
     * POST /api/tokenAddon/create
     * { "addonId": "addon-growth-100k" }
     */
    @PostMapping("/create")
    public AjaxResult createAddonOrder(@RequestBody Map<String, String> params) {
        String addonId = params.get("addonId");
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(addonService.createAddonOrder(userId, addonId));
    }

    /**
     * 加购包支付成功回调（内部调用，支付回调时触发）
     */
    @PostMapping("/confirm")
    public AjaxResult confirmAddon(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        addonService.confirmAddonPayment(orderNo);
        return AjaxResult.success();
    }
}
