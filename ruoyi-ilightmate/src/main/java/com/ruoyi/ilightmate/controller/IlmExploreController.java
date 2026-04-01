package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmExploreScore;
import com.ruoyi.ilightmate.mapper.IlmExploreScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 七维探索 Controller
 *
 * GET  /api/explore/scores            — 获取用户七维分数
 * POST /api/explore/scores            — 创建或更新七维分数
 */
@RestController
@RequestMapping("/api/explore")
public class IlmExploreController {

    @Autowired
    private IlmExploreScoreMapper scoreMapper;

    @GetMapping("/scores")
    public AjaxResult getScores() {
        Long userId = SecurityUtils.getUserId();
        IlmExploreScore score = scoreMapper.selectByUserId(userId);
        if (score == null) {
            // 返回默认分数
            score = new IlmExploreScore();
            score.setUserId(userId);
            score.setDimAndun(50);
            score.setDimLijie(50);
            score.setDimQingxu(50);
            score.setDimLianjie(50);
            score.setDimXingdong(50);
            score.setDimJiena(50);
            score.setDimJuecha(50);
        }
        return AjaxResult.success(score);
    }

    @PostMapping("/scores")
    public AjaxResult updateScores(@RequestBody Map<String, Integer> scores) {
        Long userId = SecurityUtils.getUserId();
        IlmExploreScore existing = scoreMapper.selectByUserId(userId);

        IlmExploreScore score = existing != null ? existing : new IlmExploreScore();
        score.setUserId(userId);
        score.setDimAndun(scores.getOrDefault("安顿", score.getDimAndun() != null ? score.getDimAndun() : 50));
        score.setDimLijie(scores.getOrDefault("理解", score.getDimLijie() != null ? score.getDimLijie() : 50));
        score.setDimQingxu(scores.getOrDefault("情绪流动", score.getDimQingxu() != null ? score.getDimQingxu() : 50));
        score.setDimLianjie(scores.getOrDefault("连结", score.getDimLianjie() != null ? score.getDimLianjie() : 50));
        score.setDimXingdong(scores.getOrDefault("行动", score.getDimXingdong() != null ? score.getDimXingdong() : 50));
        score.setDimJiena(scores.getOrDefault("接纳", score.getDimJiena() != null ? score.getDimJiena() : 50));
        score.setDimJuecha(scores.getOrDefault("觉察", score.getDimJuecha() != null ? score.getDimJuecha() : 50));

        if (existing != null) {
            scoreMapper.updateByUserId(score);
        } else {
            scoreMapper.insert(score);
        }

        return AjaxResult.success(score);
    }
}
