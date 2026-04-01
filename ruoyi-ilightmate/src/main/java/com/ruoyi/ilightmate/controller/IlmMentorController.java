package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmPartner;
import com.ruoyi.ilightmate.service.IIlmMentorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 导师管理 Controller
 *
 * 面向专业版用户（导师/引导师）：
 *   POST /api/mentor/register            — 注册为导师（生成 MT 码）
 *   GET  /api/mentor/info                — 获取导师信息（含 MT 码）
 *   GET  /api/mentor/clients             — 获取名下客户列表
 *   GET  /api/mentor/stats               — 获取导师统计
 *   POST /api/mentor/bind                — 客户绑定导师 MT 码
 *
 * 面向管理后台：
 *   GET  /api/mentor/list                — 所有导师列表
 */
@RestController
@RequestMapping("/api/mentor")
public class IlmMentorController {

    @Autowired
    private IIlmMentorService mentorService;

    /**
     * 注册为导师
     * 专业版用户在设置页或首次使用导师功能时调用
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            return AjaxResult.error("导师姓名不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        String phone = params.get("phone");

        IlmPartner partner = mentorService.registerAsMentor(userId, name, phone);
        return AjaxResult.success(partner);
    }

    /**
     * 获取当前登录用户的导师信息（含 MT 码）
     */
    @GetMapping("/info")
    public AjaxResult getInfo() {
        Long userId = SecurityUtils.getUserId();
        IlmPartner mentor = mentorService.getMentorInfo(userId);
        if (mentor == null) {
            return AjaxResult.error("您尚未注册为导师");
        }
        return AjaxResult.success(mentor);
    }

    /**
     * 获取导师名下的客户列表
     * 返回：userId, nickName, phone, registeredAt, hasPaid, locked
     */
    @GetMapping("/clients")
    public AjaxResult getClients() {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> clients = mentorService.getMentorClients(userId);
        return AjaxResult.success(clients);
    }

    /**
     * 获取导师统计数据
     */
    @GetMapping("/stats")
    public AjaxResult getStats() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> stats = mentorService.getMentorStats(userId);
        return AjaxResult.success(stats);
    }

    /**
     * 客户绑定导师 MT 码
     * 客户端调用，传入 MT-XXXX 码
     */
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getUserId();
        String mtCode = params.get("mtCode");
        if (mtCode == null || !mtCode.startsWith("MT-")) {
            return AjaxResult.error("请输入有效的导师邀请码（MT-XXXX）");
        }

        try {
            mentorService.bindMentorCode(userId, mtCode);
            return AjaxResult.success("已成功绑定导师");
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
