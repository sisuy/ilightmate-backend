package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.ilightmate.service.IIlmAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 行为埋点 Controller
 *
 * 匹配前端 src/lib/behavioral-events.ts 的 flushEventsToBackend()
 * Best-effort 存储，不影响用户体验
 */
@RestController
@RequestMapping("/system/analytics")
public class IlmAnalyticsController {

    @Autowired
    private IIlmAnalyticsService analyticsService;

    /**
     * 批量上报行为事件
     * POST /system/analytics/events
     */
    @PostMapping("/events")
    public AjaxResult flushEvents(@RequestBody Map<String, Object> payload) {
        try {
            String sessionId = (String) payload.get("sessionId");
            List<Map<String, Object>> events = (List<Map<String, Object>>) payload.get("events");
            String timestamp = (String) payload.get("timestamp");

            analyticsService.batchInsert(sessionId, events);
            return AjaxResult.success();
        } catch (Exception e) {
            // best-effort: 失败不影响前端
            return AjaxResult.success();
        }
    }
}
