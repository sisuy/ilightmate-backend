package com.ruoyi.ilightmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置
 *
 * ilightmate.wechat:
 *   app-id: ${WECHAT_APP_ID}
 *   mch-id: ${WECHAT_MCH_ID}
 *   api-key-v3: ${WECHAT_API_KEY_V3}
 *   cert-serial-no: ${WECHAT_CERT_SERIAL_NO}
 *   private-key-path: /path/to/apiclient_key.pem
 *   notify-url: https://api.ilightmate.cn/api/pay/wechat/notify
 */
@Component
@ConfigurationProperties(prefix = "ilightmate.wechat")
public class WechatPayConfig {

    private String appId;
    private String mchId;
    private String apiKeyV3;
    private String certSerialNo;
    private String privateKeyPath;
    private String notifyUrl;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getMchId() { return mchId; }
    public void setMchId(String mchId) { this.mchId = mchId; }
    public String getApiKeyV3() { return apiKeyV3; }
    public void setApiKeyV3(String apiKeyV3) { this.apiKeyV3 = apiKeyV3; }
    public String getCertSerialNo() { return certSerialNo; }
    public void setCertSerialNo(String certSerialNo) { this.certSerialNo = certSerialNo; }
    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
}
