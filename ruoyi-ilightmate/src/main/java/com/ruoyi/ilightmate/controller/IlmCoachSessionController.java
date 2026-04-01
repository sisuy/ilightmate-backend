package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmCoachSession;
import com.ruoyi.ilightmate.mapper.IlmCoachSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 明场教练会话 Controller
 *
 * POST   /api/coach/session          — 创建/保存明场会话
 * PUT    /api/coach/session/{id}     — 更新会话进度
 * GET    /api/coach/sessions         — 列出用户所有会话
 * GET    /api/coach/session/active   — 获取当前未完成的会话
 * GET    /api/coach/session/{id}     — 获取单个会话
 * DELETE /api/coach/session/{id}     — 删除会话
 */
@RestController
@RequestMapping("/api/coach")
public class IlmCoachSessionController {

    @Autowired
    private IlmCoachSessionMapper sessionMapper;

    @PostMapping("/session")
    public AjaxResult create(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();

        IlmCoachSession session = new IlmCoachSession();
        session.setUserId(userId);
        session.setTheme((String) params.get("theme"));
        session.setSubTopic((String) params.get("subTopic"));
        session.setCurrentStep(1);
        session.setCompleted(false);
        session.setRadarBefore(toJsonString(params.get("radarBefore")));

        sessionMapper.insert(session);
        return AjaxResult.success(session);
    }

    @PutMapping("/session/{id}")
    public AjaxResult update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        IlmCoachSession existing = sessionMapper.selectByIdAndUserId(id, userId);
        if (existing == null) return AjaxResult.error("会话不存在");

        // Update fields that are present
        if (params.containsKey("currentStep")) existing.setCurrentStep(((Number) params.get("currentStep")).intValue());
        if (params.containsKey("subStep")) existing.setSubStep((String) params.get("subStep"));
        if (params.containsKey("analysisData")) existing.setAnalysisData(toJsonString(params.get("analysisData")));
        if (params.containsKey("insightData")) existing.setInsightData(toJsonString(params.get("insightData")));
        if (params.containsKey("selectedCompanion")) existing.setSelectedCompanion((String) params.get("selectedCompanion"));
        if (params.containsKey("companionDialogueData")) existing.setCompanionDialogueData(toJsonString(params.get("companionDialogueData")));
        if (params.containsKey("actionCommitment")) existing.setActionCommitment((String) params.get("actionCommitment"));
        if (params.containsKey("growthCycle")) existing.setGrowthCycle(toJsonString(params.get("growthCycle")));
        if (params.containsKey("radarBefore")) existing.setRadarBefore(toJsonString(params.get("radarBefore")));
        if (params.containsKey("completed")) existing.setCompleted((Boolean) params.get("completed"));

        sessionMapper.update(existing);
        return AjaxResult.success(existing);
    }

    @GetMapping("/sessions")
    public AjaxResult list() {
        Long userId = SecurityUtils.getUserId();
        List<IlmCoachSession> sessions = sessionMapper.selectByUserId(userId);
        return AjaxResult.success(sessions);
    }

    @GetMapping("/session/active")
    public AjaxResult getActive() {
        Long userId = SecurityUtils.getUserId();
        IlmCoachSession session = sessionMapper.selectActiveByUserId(userId);
        return AjaxResult.success(session);
    }

    @GetMapping("/session/{id}")
    public AjaxResult get(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        IlmCoachSession session = sessionMapper.selectByIdAndUserId(id, userId);
        if (session == null) return AjaxResult.error("会话不存在");
        return AjaxResult.success(session);
    }

    @DeleteMapping("/session/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        sessionMapper.deleteByIdAndUserId(id, userId);
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
