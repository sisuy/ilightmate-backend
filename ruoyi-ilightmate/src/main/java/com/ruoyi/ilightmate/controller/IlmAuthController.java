package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.ilightmate.service.IIlmSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SMS 登录 Controller
 *
 * 匹配前端 src/api/auth.ts 的 getSmsCode + loginWithPhoneCode
 */
@RestController
public class IlmAuthController {

    @Autowired
    private IIlmSmsService smsService;

    /**
     * 发送 SMS 验证码
     * GET /resource/sms/code/login?phonenumber=13800000000
     */
    @GetMapping("/resource/sms/code/login")
    public AjaxResult sendSmsCode(@RequestParam String phonenumber) {
        try {
            smsService.sendVerificationCode(phonenumber);
            return AjaxResult.success("验证码已发送");
        } catch (Exception e) {
            return AjaxResult.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * SMS 验证码登录
     * POST /auth/smsLogin
     */
    @PostMapping("/auth/smsLogin")
    public AjaxResult smsLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phonenumber");
        String smsCode = params.get("smsCode");

        try {
            Map<String, Object> result = smsService.verifyAndLogin(phone, smsCode);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 绑定推荐码
     * POST /system/user/bind-referral-code
     */
    @PostMapping("/system/user/bind-referral-code")
    public AjaxResult bindReferralCode(@RequestBody Map<String, String> params) {
        String referralCode = params.get("referralCode");
        try {
            Map<String, Object> result = smsService.bindReferralCode(referralCode);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("绑定失败: " + e.getMessage());
        }
    }
}
