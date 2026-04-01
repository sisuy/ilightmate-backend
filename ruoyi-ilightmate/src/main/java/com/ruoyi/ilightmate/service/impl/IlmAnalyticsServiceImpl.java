package com.ruoyi.ilightmate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.mapper.IlmBehavioralEventMapper;
import com.ruoyi.ilightmate.service.IIlmAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IlmAnalyticsServiceImpl implements IIlmAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(IlmAnalyticsServiceImpl.class);

    @Autowired
    private IlmBehavioralEventMapper eventMapper;

    @Override
    @Async
    public void batchInsert(String sessionId, List<Map<String, Object>> events) {
        if (events == null || events.isEmpty()) return;

        Long userId = null;
        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            // 匿名用户
        }

        for (Map<String, Object> event : events) {
            try {
                eventMapper.insert(
                        sessionId,
                        userId,
                        (String) event.get("eventType"),
                        (String) event.get("companionId"),
                        JSON.toJSONString(event.get("payload")),
                        (String) event.get("timestamp")
                );
            } catch (Exception e) {
                log.debug("Skip event insert: {}", e.getMessage());
            }
        }
        log.debug("Flushed {} behavioral events for session {}", events.size(), sessionId);
    }
}
