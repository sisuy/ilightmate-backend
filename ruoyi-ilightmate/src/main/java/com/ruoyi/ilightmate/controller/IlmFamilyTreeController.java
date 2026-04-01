package com.ruoyi.ilightmate.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.ilightmate.domain.IlmFamilyTree;
import com.ruoyi.ilightmate.service.IIlmFamilyTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家族树 Controller
 *
 * 前端对接：
 *   GET    /api/family-tree/list           — 用户所有家族树列表（不含 treeData）
 *   GET    /api/family-tree/{treeId}       — 获取单棵家族树（含完整 treeData）
 *   POST   /api/family-tree/create         — 创建家族树
 *   PUT    /api/family-tree/{treeId}       — 更新家族树
 *   PUT    /api/family-tree/{treeId}/analysis — 保存七层分析结果
 *   DELETE /api/family-tree/{treeId}       — 删除家族树
 */
@RestController
@RequestMapping("/api/family-tree")
public class IlmFamilyTreeController {

    @Autowired
    private IIlmFamilyTreeService treeService;

    @GetMapping("/list")
    public AjaxResult list() {
        Long userId = SecurityUtils.getUserId();
        List<IlmFamilyTree> trees = treeService.listByUserId(userId);
        return AjaxResult.success(trees);
    }

    @GetMapping("/{treeId}")
    public AjaxResult get(@PathVariable Long treeId) {
        Long userId = SecurityUtils.getUserId();
        IlmFamilyTree tree = treeService.getById(userId, treeId);
        return AjaxResult.success(tree);
    }

    @PostMapping("/create")
    public AjaxResult create(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        String treeName = (String) params.getOrDefault("treeName", "我的家族树");
        String treeData = (String) params.get("treeData");
        int memberCount = params.containsKey("memberCount") ? ((Number) params.get("memberCount")).intValue() : 0;
        int generationCount = params.containsKey("generationCount") ? ((Number) params.get("generationCount")).intValue() : 0;

        IlmFamilyTree tree = treeService.create(userId, treeName, treeData, memberCount, generationCount);
        return AjaxResult.success(tree);
    }

    @PutMapping("/{treeId}")
    public AjaxResult update(@PathVariable Long treeId, @RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        String treeName = (String) params.getOrDefault("treeName", "我的家族树");
        String treeData = (String) params.get("treeData");
        int memberCount = params.containsKey("memberCount") ? ((Number) params.get("memberCount")).intValue() : 0;
        int generationCount = params.containsKey("generationCount") ? ((Number) params.get("generationCount")).intValue() : 0;

        treeService.update(userId, treeId, treeName, treeData, memberCount, generationCount);
        return AjaxResult.success();
    }

    @PutMapping("/{treeId}/analysis")
    public AjaxResult updateAnalysis(@PathVariable Long treeId, @RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getUserId();
        treeService.updateAnalysis(userId, treeId,
                params.get("analysisData"),
                params.get("patterns"),
                params.get("familyType"),
                params.get("direction"),
                params.get("riskLevel"));
        return AjaxResult.success();
    }

    @DeleteMapping("/{treeId}")
    public AjaxResult delete(@PathVariable Long treeId) {
        Long userId = SecurityUtils.getUserId();
        treeService.delete(userId, treeId);
        return AjaxResult.success();
    }
}
