package com.ruoyi.ilightmate.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IIlmPayService {

    /** 创建支付宝 PC 页面支付 */
    Map<String, Object> createAlipayPage(String orderNo, String amountYuan);

    /** 创建微信 Native 扫码支付 */
    Map<String, Object> createWechatNative(String orderNo, String amountYuan);

    /** 查询支付宝订单状态 */
    Map<String, Object> queryAlipay(String outTradeNo, String tradeNo);

    /** 查询微信订单状态 */
    Map<String, Object> queryWechat(String outTradeNo);

    /** 处理支付宝异步回调 */
    String handleAlipayNotify(HttpServletRequest request);

    /** 处理微信异步回调 */
    String handleWechatNotify(HttpServletRequest request);

    /** 关闭支付宝订单 */
    void closeAlipayOrder(String outTradeNo, String tradeNo);
}
