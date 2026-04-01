package com.ruoyi.ilightmate.service;

import com.ruoyi.ilightmate.domain.IlmFamilyTree;

import java.util.List;

public interface IIlmFamilyTreeService {
    IlmFamilyTree create(Long userId, String treeName, String treeData, int memberCount, int generationCount);
    IlmFamilyTree getById(Long userId, Long treeId);
    List<IlmFamilyTree> listByUserId(Long userId);
    void update(Long userId, Long treeId, String treeName, String treeData, int memberCount, int generationCount);
    void updateAnalysis(Long userId, Long treeId, String analysisData, String patterns, String familyType, String direction, String riskLevel);
    void delete(Long userId, Long treeId);
}
