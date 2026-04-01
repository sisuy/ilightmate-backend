package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.config.SmsConfig;
import com.ruoyi.ilightmate.mapper.IlmUserAttributionMapper;
import com.ruoyi.ilightmate.service.IIlmSmsService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.common.core.domain.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IlmSmsServiceImpl implements IIlmSmsService {

    private static final Logger log = LoggerFactory.getLogger(IlmSmsServiceImpl.class);
    private static final String SMS_CODE_PREFIX = "ilm:sms:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired(required = false)
    private IlmUserAttributionMapper attributionMapper;

    @Override
    public void sendVerificationCode(String phone) throws Exception {
        // 1. 生成 6 位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 2. 存入 Redis（5 分钟过期）
        redisCache.setCacheObject(SMS_CODE_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 3. 通过阿里云 SMS 发送
        if (smsConfig.isEnabled()) {
            sendAliyunSms(phone, code);
        } else {
            // 开发模式：打印到日志
            log.info("[DEV MODE] SMS code for {}: {}", phone, code);
        }
    }

    @Override
    public Map<String, Object> verifyAndLogin(String phone, String smsCode) throws Exception {
        // 1. 验证码校验
        String cachedCode = redisCache.getCacheObject(SMS_CODE_PREFIX + phone);
        if (cachedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(smsCode)) {
            throw new RuntimeException("验证码错误");
        }

        // 验证通过，清除 Redis
        redisCache.deleteObject(SMS_CODE_PREFIX + phone);

        // 2. 查找或创建用户
        SysUser user = userService.selectUserByPhoneNumber(phone);
        if (user == null) {
            // 自动注册
            user = new SysUser();
            user.setPhonenumber(phone);
            user.setUserName(phone); // 用手机号作为默认用户名
            user.setNickName("用户" + phone.substring(phone.length() - 4));
            user.setPassword(SecurityUtils.encryptPassword("ilm_default_" + phone)); // 默认密码
            userService.insertUser(user);
            user = userService.selectUserByPhoneNumber(phone); // 重新查询获取 ID
        }

        // 3. 签发 JWT（复用 RuoYi 的 TokenService）
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUser(user);
        String token = tokenService.createToken(loginUser);

        // 4. 构建返回
        Map<String, Object> result = new HashMap<>();
        result.put("access_token", token);
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("userName", user.getUserName());
        userInfo.put("nickName", user.getNickName());
        userInfo.put("phonenumber", user.getPhonenumber());
        result.put("userInfo", userInfo);

        return result;
    }

    @Override
    public Map<String, Object> bindReferralCode(String referralCode) throws Exception {
        Long userId = SecurityUtils.getUserId();

        // 检查是否已锁定
        if (attributionMapper != null) {
            Boolean locked = attributionMapper.isLocked(userId);
            if (locked != null && locked) {
                throw new RuntimeException("推荐码已锁定，无法更改");
            }
            attributionMapper.upsertAttribution(userId, referralCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "推荐码绑定成功");
        return result;
    }

    // ── 阿里云 SMS ──

    private void sendAliyunSms(String phone, String code) throws Exception {
        try {
            com.aliyun.dysmsapi20170525.Client client = createSmsClient();
            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest =
                    new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                            .setPhoneNumbers(phone)
                            .setSignName(smsConfig.getSignName())
                            .setTemplateCode(smsConfig.getTemplateCode())
                            .setTemplateParam("{\"code\":\"" + code + "\"}");
            client.sendSms(sendSmsRequest);
            log.info("SMS sent to {} via Aliyun", phone);
        } catch (Exception e) {
            log.error("Aliyun SMS failed for {}: {}", phone, e.getMessage());
            throw new RuntimeException("短信发送失败，请稍后重试");
        }
    }

    private com.aliyun.dysmsapi20170525.Client createSmsClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(smsConfig.getAccessKeyId())
                .setAccessKeySecret(smsConfig.getAccessKeySecret())
                .setEndpoint("dysmsapi.aliyuncs.com");
        return new com.aliyun.dysmsapi20170525.Client(config);
    }
}
