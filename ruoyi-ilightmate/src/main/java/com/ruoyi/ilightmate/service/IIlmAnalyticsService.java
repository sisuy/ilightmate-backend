package com.ruoyi.ilightmate.service;

import java.util.List;
import java.util.Map;

public interface IIlmAnalyticsService {

    /** 批量插入行为事件 */
    void batchInsert(String sessionId, List<Map<String, Object>> events);
}
