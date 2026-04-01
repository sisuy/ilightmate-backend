package com.ruoyi.ilightmate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 SMS 配置
 *
 * ilightmate.sms:
 *   enabled: true
 *   access-key-id: ${ALIYUN_ACCESS_KEY_ID}
 *   access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
 *   sign-name: 知见光伙伴
 *   template-code: SMS_XXXXXXX
 */
@Component
@ConfigurationProperties(prefix = "ilightmate.sms")
public class SmsConfig {

    private boolean enabled = false;
    private String accessKeyId;
    private String accessKeySecret;
    private String signName = "知见光伙伴";
    private String templateCode;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
    public String getAccessKeySecret() { return accessKeySecret; }
    public void setAccessKeySecret(String accessKeySecret) { this.accessKeySecret = accessKeySecret; }
    public String getSignName() { return signName; }
    public void setSignName(String signName) { this.signName = signName; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
}
