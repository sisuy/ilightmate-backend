package com.ruoyi.ilightmate.service;

import java.util.Map;

public interface IIlmSmsService {

    /** 发送验证码到手机 */
    void sendVerificationCode(String phone) throws Exception;

    /** 验证码登录（验证 + 自动注册 + JWT 签发） */
    Map<String, Object> verifyAndLogin(String phone, String smsCode) throws Exception;

    /** 绑定推荐码 */
    Map<String, Object> bindReferralCode(String referralCode) throws Exception;
}
