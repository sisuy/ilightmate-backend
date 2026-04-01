package com.ruoyi.ilightmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置
 *
 * ilightmate.alipay:
 *   app-id: ${ALIPAY_APP_ID}
 *   private-key: ${ALIPAY_PRIVATE_KEY}
 *   public-key: ${ALIPAY_PUBLIC_KEY}
 *   gateway-url: https://openapi.alipay.com/gateway.do
 *   notify-url: https://api.ilightmate.cn/api/pay/alipay/notify
 *   return-url: https://app.ilightmate.cn/app/pricing
 */
@Component
@ConfigurationProperties(prefix = "ilightmate.alipay")
public class AlipayConfig {

    private String appId;
    private String privateKey;
    private String publicKey;
    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";
    private String notifyUrl;
    private String returnUrl;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getGatewayUrl() { return gatewayUrl; }
    public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
