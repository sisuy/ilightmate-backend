package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmPartner;
import com.ruoyi.ilightmate.domain.IlmUserAttribution;

import java.util.List;
import java.util.Map;

/**
 * 导师服务
 *
 * 核心逻辑：
 * 1. 导师注册：创建 partner 记录 + MT- 码
 * 2. MT 码绑定：客户扫码/输入 → 归因记录
 * 3. 客户列表：导师查看自己名下客户
 * 4. Token 奖励：客户首次付费 → 导师 +200K token
 */
public interface IIlmMentorService {

    /** 为当前登录用户创建导师身份（生成 MT 码） */
    IlmPartner registerAsMentor(Long userId, String name, String phone);

    /** 获取导师自己的 partner 信息（含 MT 码） */
    IlmPartner getMentorInfo(Long userId);

    /** 获取导师名下的客户列表（通过归因表关联） */
    List<Map<String, Object>> getMentorClients(Long mentorUserId);

    /** 客户绑定导师 MT 码 */
    void bindMentorCode(Long clientUserId, String mtCode);

    /** 获取导师客户统计 */
    Map<String, Object> getMentorStats(Long mentorUserId);
}
