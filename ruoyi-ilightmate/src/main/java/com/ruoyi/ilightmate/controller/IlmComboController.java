package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.ilightmate.domain.IlmComboPlan;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.service.IIlmComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 套餐管理 Controller
 *
 * 匹配前端 src/api/combo.ts 的所有调用
 */
@RestController
public class IlmComboController {

    @Autowired
    private IIlmComboService comboService;

    /**
     * 获取用户当前套餐
     * GET /consultant/user/combo/{userId}
     * 返回 ComboInfo 格式
     */
    @GetMapping("/consultant/user/combo/{userId}")
    public AjaxResult getComboByUserId(@PathVariable Long userId) {
        IlmUserSubscription sub = comboService.getActiveSubscription(userId);
        if (sub == null) {
            // 无订阅 = 体验版
            Map<String, Object> freeCombo = new HashMap<>();
            freeCombo.put("code", "0");
            freeCombo.put("name", "体验版");
            freeCombo.put("type", "FREE");
            freeCombo.put("dailyConsultLimit", 5);
            freeCombo.put("startTime", null);
            freeCombo.put("endTime", null);
            return AjaxResult.success(freeCombo);
        }

        Map<String, Object> comboInfo = new HashMap<>();
        comboInfo.put("code", sub.getComboCode());
        comboInfo.put("name", sub.getComboName());
        comboInfo.put("type", sub.getComboType());
        comboInfo.put("dailyConsultLimit", sub.getDailyConsultLimit());
        comboInfo.put("startTime", sub.getStartTime());
        comboInfo.put("endTime", sub.getEndTime());
        return AjaxResult.success(comboInfo);
    }

    /**
     * 获取所有可用套餐
     * GET /api/combo/query
     * 返回 ComboListInfo[]
     */
    @GetMapping("/api/combo/query")
    public AjaxResult getComboList() {
        List<IlmComboPlan> plans = comboService.listActivePlans();
        return AjaxResult.success(plans);
    }

    /**
     * 获取套餐权益详情
     * GET /consultant/combo/{comboId}/benefits
     */
    @GetMapping("/consultant/combo/{comboId}/benefits")
    public AjaxResult getComboBenefits(@PathVariable Long comboId) {
        IlmComboPlan plan = comboService.getPlanById(comboId);
        if (plan == null) {
            return AjaxResult.error("套餐不存在");
        }
        return AjaxResult.success(plan);
    }
}
