package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.ilightmate.service.IIlmPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付 Controller
 *
 * 匹配前端 src/api/payment.ts 的支付宝 + 微信支付调用
 */
@RestController
@RequestMapping("/api/pay")
public class IlmPayController {

    @Autowired
    private IIlmPayService payService;

    /**
     * 支付宝 PC 页面支付
     * POST /api/pay/alipay/page
     */
    @PostMapping("/alipay/page")
    public AjaxResult alipayPage(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String amountYuan = params.get("amountYuan");
        Map<String, Object> result = payService.createAlipayPage(orderNo, amountYuan);
        return AjaxResult.success(result);
    }

    /**
     * 微信支付 Native（PC 扫码）
     * POST /api/pay/wechat/pc
     */
    @PostMapping("/wechat/pc")
    public AjaxResult wechatPc(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        String amountYuan = (String) params.get("amountYuan");
        Map<String, Object> result = payService.createWechatNative(orderNo, amountYuan);
        return AjaxResult.success(result);
    }

    /**
     * 支付宝支付结果查询
     * GET /api/pay/alipay/query?outTradeNo=X
     */
    @GetMapping("/alipay/query")
    public AjaxResult alipayQuery(@RequestParam(required = false) String outTradeNo,
                                   @RequestParam(required = false) String tradeNo) {
        Map<String, Object> result = payService.queryAlipay(outTradeNo, tradeNo);
        return AjaxResult.success(result);
    }

    /**
     * 微信支付结果查询
     * GET /api/pay/wechat/query?outTradeNo=X
     */
    @GetMapping("/wechat/query")
    public AjaxResult wechatQuery(@RequestParam String outTradeNo) {
        Map<String, Object> result = payService.queryWechat(outTradeNo);
        return AjaxResult.success(result);
    }

    /**
     * 支付宝异步回调
     * POST /api/pay/alipay/notify
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        return payService.handleAlipayNotify(request);
    }

    /**
     * 微信支付异步回调
     * POST /api/pay/wechat/notify
     */
    @PostMapping("/wechat/notify")
    public String wechatNotify(HttpServletRequest request) {
        return payService.handleWechatNotify(request);
    }

    /**
     * 关闭支付宝订单
     * GET /api/pay/alipay/close
     */
    @GetMapping("/alipay/close")
    public AjaxResult alipayClose(@RequestParam(required = false) String outTradeNo,
                                   @RequestParam(required = false) String tradeNo) {
        payService.closeAlipayOrder(outTradeNo, tradeNo);
        return AjaxResult.success();
    }
}
