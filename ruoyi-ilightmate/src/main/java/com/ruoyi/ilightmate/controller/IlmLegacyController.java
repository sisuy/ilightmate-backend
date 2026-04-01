package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmLegacyMember;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmLegacyMemberMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 光语传承 Controller
 *
 * POST   /api/legacy/member          — 创建传承成员
 * GET    /api/legacy/members         — 列出传承成员
 * GET    /api/legacy/member/{id}     — 获取单个传承成员
 * DELETE /api/legacy/member/{id}     — 删除传承成员
 */
@RestController
@RequestMapping("/api/legacy")
public class IlmLegacyController {

    /** 传承成员上限：成长版3人，专业版无限 */
    private static final int GROWTH_LEGACY_LIMIT = 3;

    @Autowired
    private IlmLegacyMemberMapper legacyMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @PostMapping("/member")
    public AjaxResult create(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();

        // 检查传承成员上限
        int count = legacyMapper.countByUserId(userId);
        IlmUserSubscription sub = subscriptionMapper.selectActiveByUserId(userId);
        String code = sub != null ? sub.getComboCode() : "0";
        if ("0".equals(code)) {
            return AjaxResult.error("体验版不支持光语传承，请升级成长版");
        }
        if ("1".equals(code) && count >= GROWTH_LEGACY_LIMIT) {
            return AjaxResult.error("成长版最多创建" + GROWTH_LEGACY_LIMIT + "位传承成员，请升级专业版");
        }

        String name = (String) params.get("name");
        String role = (String) params.get("role");
        if (name == null || name.trim().isEmpty()) {
            return AjaxResult.error("传承成员姓名不能为空");
        }
        if (role == null || role.trim().isEmpty()) {
            return AjaxResult.error("传承成员角色不能为空");
        }

        IlmLegacyMember member = new IlmLegacyMember();
        member.setUserId(userId);
        member.setName(name);
        member.setRole(role);
        member.setToneStyle((String) params.get("toneStyle"));
        member.setAtmosphere((String) params.get("atmosphere"));
        member.setCatchphrases(toJsonString(params.get("catchphrases")));

        legacyMapper.insert(member);
        return AjaxResult.success(member);
    }

    @GetMapping("/members")
    public AjaxResult list() {
        Long userId = SecurityUtils.getUserId();
        List<IlmLegacyMember> members = legacyMapper.selectByUserId(userId);
        return AjaxResult.success(members);
    }

    @GetMapping("/member/{id}")
    public AjaxResult get(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        IlmLegacyMember member = legacyMapper.selectByIdAndUserId(id, userId);
        if (member == null) return AjaxResult.error("传承成员不存在");
        return AjaxResult.success(member);
    }

    @DeleteMapping("/member/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        legacyMapper.deleteByIdAndUserId(id, userId);
        return AjaxResult.success();
    }

    private String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
