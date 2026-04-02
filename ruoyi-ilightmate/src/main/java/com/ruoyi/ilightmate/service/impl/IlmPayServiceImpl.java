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
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private RSAAutoCertificateConfig wechatSdkConfig;
    private NativePayService nativePayService;
    private NotificationParser notificationParser;

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
        initWechatSdk();
    }

    /**
     * 初始化微信支付 SDK (RSAAutoCertificateConfig)
     */
    private void initWechatSdk() {
        try {
            String keyPath = wechatConfig.getPrivateKeyPath();
            if (keyPath == null || !Files.exists(Paths.get(keyPath))) {
                log.warn("微信支付私钥文件不存在: {}，微信支付功能不可用", keyPath);
                return;
            }
            wechatSdkConfig = new RSAAutoCertificateConfig.Builder()
                    .merchantId(wechatConfig.getMchId())
                    .privateKeyFromPath(wechatConfig.getPrivateKeyPath())
                    .merchantSerialNumber(wechatConfig.getCertSerialNo())
                    .apiV3Key(wechatConfig.getApiKeyV3())
                    .build();
            nativePayService = new NativePayService.Builder().config(wechatSdkConfig).build();
            notificationParser = new NotificationParser(wechatSdkConfig);
            log.info("微信支付 SDK 初始化成功, mchId={}", wechatConfig.getMchId());
        } catch (Exception e) {
            log.error("微信支付 SDK 初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付 SDK 初始化失败: " + e.getMessage(), e);
        }
    }

    private void ensureWechatReady() {
        if (nativePayService == null || wechatSdkConfig == null) {
            throw new RuntimeException("微信支付 SDK 未初始化，请检查私钥文件配置");
        }
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
        ensureWechatReady();
        Map<String, Object> result = new HashMap<>();
        try {
            int amountFen = (int) (Double.parseDouble(amountYuan) * 100);
            PrepayRequest request = buildNativePrepayRequest(orderNo, amountFen);
            PrepayResponse response = nativePayService.prepay(request);

            result.put("orderNo", orderNo);
            result.put("payType", "WECHAT");
            result.put("payUrl", response.getCodeUrl());
            log.info("WeChat Native pay created for order {} amount {} fen", orderNo, amountFen);
        } catch (Exception e) {
            log.error("WeChat pay error: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付创建失败: " + e.getMessage(), e);
        }
        return result;
    }

    private PrepayRequest buildNativePrepayRequest(String orderNo, int amountFen) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(wechatConfig.getAppId());
        request.setMchid(wechatConfig.getMchId());
        request.setDescription("iLightMate 知见光伙伴");
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(wechatConfig.getNotifyUrl());
        Amount amount = new Amount();
        amount.setTotal(amountFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        return request;
    }

    @Override
    public Map<String, Object> queryWechat(String outTradeNo) {
        ensureWechatReady();
        Map<String, Object> result = new HashMap<>();
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(wechatConfig.getMchId());
            request.setOutTradeNo(outTradeNo);
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);

            result.put("outTradeNo", outTradeNo);
            result.put("tradeState", transaction.getTradeState().name());
            result.put("tradeStateDesc", transaction.getTradeStateDesc());
            if (transaction.getTransactionId() != null) {
                result.put("transactionId", transaction.getTransactionId());
            }
        } catch (Exception e) {
            log.error("WeChat query error: {}", e.getMessage(), e);
            result.put("tradeState", "ERROR");
            result.put("msg", e.getMessage());
        }
        return result;
    }

    @Override
    public String handleWechatNotify(HttpServletRequest request) {
        try {
            ensureWechatReady();
            RequestParam requestParam = buildNotifyRequestParam(request);
            Transaction transaction = notificationParser.parse(requestParam, Transaction.class);

            log.info("WeChat notify received: orderNo={} state={}", transaction.getOutTradeNo(), transaction.getTradeState());
            if (Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState())) {
                orderService.markPaid(transaction.getOutTradeNo(), "WECHAT", transaction.getTransactionId());
            }
            return "{\"code\":\"SUCCESS\",\"message\":\"OK\"}";
        } catch (Exception e) {
            log.error("WeChat notify error: {}", e.getMessage(), e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    private RequestParam buildNotifyRequestParam(HttpServletRequest request) throws Exception {
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        return new RequestParam.Builder()
                .serialNumber(request.getHeader("Wechatpay-Serial"))
                .nonce(request.getHeader("Wechatpay-Nonce"))
                .timestamp(request.getHeader("Wechatpay-Timestamp"))
                .signature(request.getHeader("Wechatpay-Signature"))
                .body(body)
                .build();
    }
}
