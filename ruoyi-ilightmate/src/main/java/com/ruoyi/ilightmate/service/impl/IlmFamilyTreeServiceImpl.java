package com.ruoyi.ilightmate.service.impl;

import com.ruoyi.ilightmate.domain.IlmFamilyTree;
import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import com.ruoyi.ilightmate.mapper.IlmFamilyTreeMapper;
import com.ruoyi.ilightmate.mapper.IlmUserSubscriptionMapper;
import com.ruoyi.ilightmate.service.IIlmFamilyTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IlmFamilyTreeServiceImpl implements IIlmFamilyTreeService {

    /** 各套餐的家族树上限 */
    private static final int FREE_TREE_LIMIT = 1;
    private static final int GROWTH_TREE_LIMIT = 3;
    private static final int PRO_TREE_LIMIT = 10;

    @Autowired
    private IlmFamilyTreeMapper treeMapper;

    @Autowired
    private IlmUserSubscriptionMapper subscriptionMapper;

    @Override
    public IlmFamilyTree create(Long userId, String treeName, String treeData, int memberCount, int generationCount) {
        // 检查树上限
        int current = treeMapper.countByUserId(userId);
        int limit = getTreeLimit(userId);
        if (current >= limit) {
            throw new RuntimeException("家族树数量已达上限（" + limit + "棵），请升级套餐");
        }

        IlmFamilyTree tree = new IlmFamilyTree();
        tree.setUserId(userId);
        tree.setTreeName(treeName != null ? treeName : "我的家族树");
        tree.setTreeData(treeData);
        tree.setMemberCount(memberCount);
        tree.setGenerationCount(generationCount);

        treeMapper.insert(tree);
        return tree;
    }

    @Override
    public IlmFamilyTree getById(Long userId, Long treeId) {
        IlmFamilyTree tree = treeMapper.selectByIdAndUserId(treeId, userId);
        if (tree == null) {
            throw new RuntimeException("家族树不存在");
        }
        return tree;
    }

    @Override
    public List<IlmFamilyTree> listByUserId(Long userId) {
        return treeMapper.selectListByUserId(userId);
    }

    @Override
    public void update(Long userId, Long treeId, String treeName, String treeData, int memberCount, int generationCount) {
        IlmFamilyTree tree = new IlmFamilyTree();
        tree.setId(treeId);
        tree.setUserId(userId);
        tree.setTreeName(treeName);
        tree.setTreeData(treeData);
        tree.setMemberCount(memberCount);
        tree.setGenerationCount(generationCount);

        int rows = treeMapper.updateTree(tree);
        if (rows == 0) {
            throw new RuntimeException("家族树不存在或无权修改");
        }
    }

    @Override
    public void updateAnalysis(Long userId, Long treeId, String analysisData, String patterns,
                                String familyType, String direction, String riskLevel) {
        IlmFamilyTree tree = new IlmFamilyTree();
        tree.setId(treeId);
        tree.setUserId(userId);
        tree.setAnalysisData(analysisData);
        tree.setPatterns(patterns);
        tree.setFamilyType(familyType);
        tree.setDirection(direction);
        tree.setRiskLevel(riskLevel);

        int rows = treeMapper.updateAnalysis(tree);
        if (rows == 0) {
            throw new RuntimeException("家族树不存在或无权修改");
        }
    }

    @Override
    public void delete(Long userId, Long treeId) {
        int rows = treeMapper.deleteByIdAndUserId(treeId, userId);
        if (rows == 0) {
            throw new RuntimeException("家族树不存在或无权删除");
        }
    }

    private int getTreeLimit(Long userId) {
        IlmUserSubscription sub = subscriptionMapper.selectActiveByUserId(userId);
        if (sub == null) return FREE_TREE_LIMIT;
        String code = sub.getComboCode();
        if ("2".equals(code)) return PRO_TREE_LIMIT;
        if ("1".equals(code)) return GROWTH_TREE_LIMIT;
        return FREE_TREE_LIMIT;
    }
}
