package com.ruoyi.ilightmate.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ruoyi.ilightmate.config.AlipayConfig;
import com.ruoyi.ilightmate.config.WechatPayConfig;
import com.ruoyi.ilightmate.service.IIlmOrderService;
import com.ruoyi.ilightmate.service.IIlmPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class IlmPayServiceImpl implements IIlmPayService {

    private static final Logger log = LoggerFactory.getLogger(IlmPayServiceImpl.class);

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private WechatPayConfig wechatConfig;

    @Autowired
    private IIlmOrderService orderService;

    private AlipayClient alipayClient;

    @PostConstruct
    public void init() {
        alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json", "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2"
        );
    }

    // ════════════════════════════════════════════════
    // 支付宝
    // ════════════════════════════════════════════════

    @Override
    public Map<String, Object> createAlipayPage(String orderNo, String amountYuan) {
        Map<String, Object> result = new HashMap<>();
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setReturnUrl(alipayConfig.getReturnUrl());
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            String bizContent = String.format(
                    "{\"out_trade_no\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"iLightMate 知见光伙伴\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                    orderNo, amountYuan
            );
            request.setBizContent(bizContent);

            String form = alipayClient.pageExecute(request).getBody();
            // form 是一个自动提交的 HTML 表单，前端可以用 window.open 打开
            // 或者我们提取 URL
            result.put("orderNo", orderNo);
            result.put("payUrl", form); // 前端用 document.write(form) 方式渲染
            result.put("payType", "ALIPAY");
            result.put("payInfo", form);
        } catch (AlipayApiException e) {
            log.error("Alipay page pay error: {}", e.getMessage());
            throw new RuntimeException("支付宝支付创建失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> queryAlipay(String outTradeNo, String tradeNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            String bizContent = "{}";
            if (outTradeNo != null) {
                bizContent = String.format("{\"out_trade_no\":\"%s\"}", outTradeNo);
            } else if (tradeNo != null) {
                bizContent = String.format("{\"trade_no\":\"%s\"}", tradeNo);
            }
            request.setBizContent(bizContent);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                result.put("tradeNo", response.getTradeNo());
                result.put("outTradeNo", response.getOutTradeNo());
                result.put("tradeStatus", response.getTradeStatus());
                result.put("totalAmount", response.getTotalAmount());
                result.put("buyerLogonId", response.getBuyerLogonId());
            } else {
                result.put("tradeStatus", "UNKNOWN");
                result.put("msg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("Alipay query error: {}", e.getMessage());
            result.put("tradeStatus", "ERROR");
        }
        return result;
    }

    @Override
    public String handleAlipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                params.put(entry.getKey(), String.join(",", entry.getValue()));
            }

            boolean verified = AlipaySignature.rsaCheckV1(
                    params, alipayConfig.getPublicKey(), "UTF-8", "RSA2"
            );

            if (!verified) {
                log.warn("Alipay notify signature verification failed");
                return "failure";
            }

            String tradeStatus = params.get("trade_status");
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                orderService.markPaid(orderNo, "ALIPAY", tradeNo);
            }

            return "success";
        } catch (Exception e) {
            log.error("Alipay notify error: {}", e.getMessage());
            return "failure";
        }
    }

    @Override
    public void closeAlipayOrder(String outTradeNo, String tradeNo) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            String bizContent = outTradeNo != null
                    ? String.format("{\"out_trade_no\":\"%s\"}", outTradeNo)
                    : String.format("{\"trade_no\":\"%s\"}", tradeNo);
            request.setBizContent(bizContent);
            alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("Alipay close error: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════
    // 微信支付
    // ════════════════════════════════════════════════

    @Override
    public Map<String, Object> createWechatNative(String orderNo, String amountYuan) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 微信支付 V3 Native 下单
            // 使用 wechatpay-java SDK
            int amountFen = (int) (Double.parseDouble(amountYuan) * 100);

            // TODO: 使用 wechatpay-java SDK 的 NativePayService
            // NativePayService service = new NativePayService.Builder().config(config).build();
            // PrepayRequest request = new PrepayRequest();
            // request.setAppid(wechatConfig.getAppId());
            // request.setMchid(wechatConfig.getMchId());
            // request.setDescription("iLightMate 知见光伙伴");
            // request.setOutTradeNo(orderNo);
            // request.setNotifyUrl(wechatConfig.getNotifyUrl());
            // Amount amount = new Amount();
            // amount.setTotal(amountFen);
            // amount.setCurrency("CNY");
            // request.setAmount(amount);
            // PrepayResponse response = service.prepay(request);
            // result.put("payUrl", response.getCodeUrl()); // 二维码链接

            // 临时返回占位（Steve 用实际 SDK 替换）
            result.put("orderNo", orderNo);
            result.put("payType", "WECHAT");
            result.put("payUrl", "weixin://wxpay/bizpayurl?pr=placeholder_" + orderNo);

            log.info("WeChat Native pay created for order {} amount {} fen", orderNo, amountFen);
        } catch (Exception e) {
            log.error("WeChat pay error: {}", e.getMessage());
            throw new RuntimeException("微信支付创建失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> queryWechat(String outTradeNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            // TODO: 使用 wechatpay-java SDK 查询
            // QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            // request.setMchid(wechatConfig.getMchId());
            // request.setOutTradeNo(outTradeNo);
            // Transaction transaction = service.queryOrderByOutTradeNo(request);
            // result.put("tradeState", transaction.getTradeState().name());

            // 临时占位
            result.put("outTradeNo", outTradeNo);
            result.put("tradeState", "NOTPAY");
            result.put("tradeStateDesc", "查询中");
        } catch (Exception e) {
            log.error("WeChat query error: {}", e.getMessage());
            result.put("tradeState", "ERROR");
        }
        return result;
    }

    @Override
    public String handleWechatNotify(HttpServletRequest request) {
        try {
            // TODO: 使用 wechatpay-java SDK 验签 + 解密
            // RequestParam requestParam = new RequestParam.Builder()
            //     .serialNumber(request.getHeader("Wechatpay-Serial"))
            //     .nonce(request.getHeader("Wechatpay-Nonce"))
            //     .timestamp(request.getHeader("Wechatpay-Timestamp"))
            //     .signature(request.getHeader("Wechatpay-Signature"))
            //     .body(StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8))
            //     .build();
            // Transaction transaction = notificationParser.parse(requestParam, Transaction.class);
            // if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
            //     orderService.markPaid(transaction.getOutTradeNo(), "WECHAT", transaction.getTransactionId());
            // }

            log.info("WeChat notify received");
            return "{\"code\":\"SUCCESS\",\"message\":\"OK\"}";
        } catch (Exception e) {
            log.error("WeChat notify error: {}", e.getMessage());
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
