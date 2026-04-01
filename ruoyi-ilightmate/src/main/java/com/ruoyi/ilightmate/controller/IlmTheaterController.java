package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmTheaterSession;
import com.ruoyi.ilightmate.mapper.IlmTheaterSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 关系剧场 Controller
 *
 * POST   /api/theater/session          — 保存剧场会话
 * GET    /api/theater/sessions         — 列出用户所有剧场会话
 * GET    /api/theater/session/{id}     — 获取单个剧场会话
 * DELETE /api/theater/session/{id}     — 删除剧场会话
 */
@RestController
@RequestMapping("/api/theater")
public class IlmTheaterController {

    @Autowired
    private IlmTheaterSessionMapper sessionMapper;

    @PostMapping("/session")
    public AjaxResult create(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        IlmTheaterSession session = new IlmTheaterSession();
        session.setUserId(userId);
        session.setTreeId((String) params.get("treeId"));
        session.setRealityPositions(toJsonString(params.get("realityPositions")));
        session.setIdealPositions(toJsonString(params.get("idealPositions")));
        session.setPatterns(toJsonString(params.get("patterns")));
        session.setCoreInsight((String) params.get("coreInsight"));
        session.setActionCommitment((String) params.get("actionCommitment"));
        session.setRadarBefore(toJsonString(params.get("radarBefore")));
        session.setRadarAfter(toJsonString(params.get("radarAfter")));

        sessionMapper.insert(session);
        return AjaxResult.success(session);
    }

    @GetMapping("/sessions")
    public AjaxResult list(@RequestParam(required = false) String treeId) {
        Long userId = SecurityUtils.getUserId();
        List<IlmTheaterSession> sessions;
        if (treeId != null && !treeId.isEmpty()) {
            sessions = sessionMapper.selectByUserIdAndTreeId(userId, treeId);
        } else {
            sessions = sessionMapper.selectByUserId(userId);
        }
        return AjaxResult.success(sessions);
    }

    @GetMapping("/session/{id}")
    public AjaxResult get(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        IlmTheaterSession session = sessionMapper.selectByIdAndUserId(id, userId);
        if (session == null) return AjaxResult.error("剧场会话不存在");
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
