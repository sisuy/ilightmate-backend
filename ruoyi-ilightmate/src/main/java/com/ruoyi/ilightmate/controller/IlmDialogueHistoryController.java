package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmDialogueHistory;
import com.ruoyi.ilightmate.mapper.IlmDialogueHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话历史 Controller
 *
 * POST /api/dialogue/history          — 保存对话摘要
 * GET  /api/dialogue/history          — 列出对话历史
 * GET  /api/dialogue/history/companion/{companionId} — 按伙伴查
 */
@RestController
@RequestMapping("/api/dialogue")
public class IlmDialogueHistoryController {

    @Autowired
    private IlmDialogueHistoryMapper historyMapper;

    @PostMapping("/history")
    public AjaxResult save(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        IlmDialogueHistory history = new IlmDialogueHistory();
        history.setUserId(userId);
        history.setCompanionId((String) params.get("companionId"));
        history.setConversationSummary((String) params.get("conversationSummary"));
        history.setKeyDiscoveries(toJsonString(params.get("keyDiscoveries")));
        history.setEmotionalTrajectory((String) params.get("emotionalTrajectory"));
        history.setCommitments(toJsonString(params.get("commitments")));
        history.setMessageCount(params.containsKey("messageCount") ? ((Number) params.get("messageCount")).intValue() : 0);
        history.setDuration(params.containsKey("duration") ? ((Number) params.get("duration")).intValue() : 0);

        historyMapper.insert(history);
        return AjaxResult.success(history);
    }

    @GetMapping("/history")
    public AjaxResult list(@RequestParam(defaultValue = "50") int limit) {
        Long userId = SecurityUtils.getUserId();
        List<IlmDialogueHistory> list = historyMapper.selectByUserId(userId, limit);
        return AjaxResult.success(list);
    }

    @GetMapping("/history/companion/{companionId}")
    public AjaxResult listByCompanion(@PathVariable String companionId,
                                       @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityUtils.getUserId();
        List<IlmDialogueHistory> list = historyMapper.selectByUserAndCompanion(userId, companionId, limit);
        return AjaxResult.success(list);
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
