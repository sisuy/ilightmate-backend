package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmPartner;
import com.ruoyi.ilightmate.domain.IlmUserAttribution;
import com.ruoyi.ilightmate.mapper.IlmPartnerMapper;
import com.ruoyi.ilightmate.mapper.IlmUserAttributionMapper;
import com.ruoyi.ilightmate.service.IIlmMentorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IlmMentorServiceImpl implements IIlmMentorService {

    private static final Logger log = LoggerFactory.getLogger(IlmMentorServiceImpl.class);

    @Autowired
    private IlmPartnerMapper partnerMapper;

    @Autowired
    private IlmUserAttributionMapper attributionMapper;

    @Override
    public IlmPartner registerAsMentor(Long userId, String name, String phone) {
        // 检查是否已注册
        IlmPartner existing = partnerMapper.selectMentorByUserId(userId);
        if (existing != null) {
            return existing; // 已注册，直接返回
        }

        // 生成唯一 MT 码
        String mtCode = generateMTCode();

        IlmPartner partner = new IlmPartner();
        partner.setPartnerType("mentor");
        partner.setName(name);
        partner.setPhone(phone);
        partner.setUserId(userId);
        partner.setReferralCode(mtCode);
        partner.setStatus("1");

        partnerMapper.insert(partner);
        log.info("Mentor registered: userId={} code={}", userId, mtCode);
        return partner;
    }

    @Override
    public IlmPartner getMentorInfo(Long userId) {
        return partnerMapper.selectMentorByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getMentorClients(Long mentorUserId) {
        // 1. 获取导师的 MT 码
        IlmPartner mentor = partnerMapper.selectMentorByUserId(mentorUserId);
        if (mentor == null) {
            return Collections.emptyList();
        }

        // 2. 查找所有通过此 MT 码归因的用户
        return attributionMapper.selectClientsByReferralCode(mentor.getReferralCode());
    }

    @Override
    public void bindMentorCode(Long clientUserId, String mtCode) {
        // 验证 MT 码有效
        IlmPartner mentor = partnerMapper.selectByReferralCode(mtCode);
        if (mentor == null || !"mentor".equals(mentor.getPartnerType())) {
            throw new RuntimeException("无效的导师邀请码");
        }

        // 检查是否已归因且锁定
        Boolean locked = attributionMapper.isLocked(clientUserId);
        if (locked != null && locked) {
            throw new RuntimeException("归因已锁定，无法更改");
        }

        // 写入/更新归因
        attributionMapper.upsertMentorAttribution(clientUserId, mtCode, mentor.getName());
        log.info("Client {} bound to mentor {} via {}", clientUserId, mentor.getUserId(), mtCode);
    }

    @Override
    public Map<String, Object> getMentorStats(Long mentorUserId) {
        IlmPartner mentor = partnerMapper.selectMentorByUserId(mentorUserId);
        if (mentor == null) {
            return Collections.emptyMap();
        }

        String code = mentor.getReferralCode();
        Map<String, Object> stats = new HashMap<>();
        stats.put("referralCode", code);
        stats.put("totalClients", attributionMapper.countByReferralCode(code));
        stats.put("paidClients", attributionMapper.countPaidByReferralCode(code));
        stats.put("registeredAt", mentor.getCreateTime());
        return stats;
    }

    /**
     * 生成唯一 MT-XXXX 码
     */
    private String generateMTCode() {
        for (int i = 0; i < 100; i++) {
            int num = 1000 + (int) (Math.random() * 9000); // 1000-9999
            String code = "MT-" + num;
            if (!partnerMapper.existsByReferralCode(code)) {
                return code;
            }
        }
        // 极端情况：用时间戳
        return "MT-" + System.currentTimeMillis() % 100000;
    }
}
