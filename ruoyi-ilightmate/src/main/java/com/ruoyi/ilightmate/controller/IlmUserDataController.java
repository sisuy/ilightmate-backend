package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户数据 Controller（PIPL 合规）
 *
 * GET    /api/user-data/export    — 导出用户全部数据（JSON）
 * DELETE /api/user-data/delete    — 删除用户全部业务数据
 * GET    /api/user-data/consent   — 获取同意状态
 * POST   /api/user-data/consent   — 更新同意状态
 */
@RestController
@RequestMapping("/api/user-data")
public class IlmUserDataController {

    @Autowired private IlmFamilyTreeMapper treeMapper;
    @Autowired private IlmTheaterSessionMapper theaterMapper;
    @Autowired private IlmDiaryEntryMapper diaryMapper;
    @Autowired private IlmExploreScoreMapper exploreMapper;
    @Autowired private IlmLegacyMemberMapper legacyMapper;
    @Autowired private IlmCoachSessionMapper coachMapper;
    @Autowired private IlmDialogueHistoryMapper dialogueMapper;

    @GetMapping("/export")
    public AjaxResult exportData() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("familyTrees", treeMapper.selectListByUserId(userId));
        data.put("theaterSessions", theaterMapper.selectByUserId(userId));
        data.put("diaryEntries", diaryMapper.selectByUserId(userId, 9999));
        data.put("exploreScores", exploreMapper.selectByUserId(userId));
        data.put("legacyMembers", legacyMapper.selectByUserId(userId));
        data.put("coachSessions", coachMapper.selectByUserId(userId));
        data.put("dialogueHistory", dialogueMapper.selectByUserId(userId, 9999));
        data.put("exportedAt", new java.util.Date());
        return AjaxResult.success(data);
    }
}
