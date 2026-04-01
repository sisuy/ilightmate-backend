package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmFamilyTree;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmFamilyTreeMapper {

    @Insert("INSERT INTO ilm_family_trees (user_id, tree_name, tree_data, member_count, generation_count, " +
            "analysis_data, patterns, family_type, direction, risk_level) " +
            "VALUES (#{userId}, #{treeName}, #{treeData}, #{memberCount}, #{generationCount}, " +
            "#{analysisData}, #{patterns}, #{familyType}, #{direction}, #{riskLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmFamilyTree tree);

    @Select("SELECT * FROM ilm_family_trees WHERE id = #{id} AND user_id = #{userId}")
    IlmFamilyTree selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT id, user_id, tree_name, member_count, generation_count, family_type, direction, " +
            "risk_level, create_time, update_time FROM ilm_family_trees " +
            "WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<IlmFamilyTree> selectListByUserId(@Param("userId") Long userId);

    @Update("UPDATE ilm_family_trees SET tree_name = #{treeName}, tree_data = #{treeData}, " +
            "member_count = #{memberCount}, generation_count = #{generationCount}, " +
            "update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int updateTree(IlmFamilyTree tree);

    @Update("UPDATE ilm_family_trees SET analysis_data = #{analysisData}, patterns = #{patterns}, " +
            "family_type = #{familyType}, direction = #{direction}, risk_level = #{riskLevel}, " +
            "update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int updateAnalysis(IlmFamilyTree tree);

    @Delete("DELETE FROM ilm_family_trees WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ilm_family_trees WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
