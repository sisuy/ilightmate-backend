package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmDiaryEntry;
import com.ruoyi.ilightmate.mapper.IlmDiaryEntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 日记 Controller
 *
 * POST   /api/diary/entry         — 保存日记
 * GET    /api/diary/entries        — 列出日记（默认最近365条）
 * GET    /api/diary/entry/{id}     — 获取单条日记
 * DELETE /api/diary/entry/{id}     — 删除日记
 */
@RestController
@RequestMapping("/api/diary")
public class IlmDiaryController {

    @Autowired
    private IlmDiaryEntryMapper diaryMapper;

    @PostMapping("/entry")
    public AjaxResult create(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return AjaxResult.error("日记内容不能为空");
        }
        int mood = params.containsKey("mood") ? ((Number) params.get("mood")).intValue() : 3;
        if (mood < 1 || mood > 5) {
            return AjaxResult.error("心情值必须在1到5之间");
        }
        Long userId = SecurityUtils.getUserId();
        IlmDiaryEntry entry = new IlmDiaryEntry();
        entry.setUserId(userId);
        entry.setContent(content);
        entry.setMood(mood);
        entry.setEmotions(toJsonString(params.get("emotions")));
        entry.setFamilyMentions(toJsonString(params.get("familyMentions")));
        entry.setAiInsight((String) params.get("aiInsight"));

        diaryMapper.insert(entry);
        return AjaxResult.success(entry);
    }

    @GetMapping("/entries")
    public AjaxResult list(@RequestParam(defaultValue = "365") int limit) {
        if (limit <= 0 || limit > 1000) {
            return AjaxResult.error("查询条数必须在1到1000之间");
        }
        Long userId = SecurityUtils.getUserId();
        List<IlmDiaryEntry> entries = diaryMapper.selectByUserId(userId, limit);
        return AjaxResult.success(entries);
    }

    @GetMapping("/entry/{id}")
    public AjaxResult get(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        IlmDiaryEntry entry = diaryMapper.selectByIdAndUserId(id, userId);
        if (entry == null) return AjaxResult.error("日记不存在");
        return AjaxResult.success(entry);
    }

    @DeleteMapping("/entry/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        diaryMapper.deleteByIdAndUserId(id, userId);
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
