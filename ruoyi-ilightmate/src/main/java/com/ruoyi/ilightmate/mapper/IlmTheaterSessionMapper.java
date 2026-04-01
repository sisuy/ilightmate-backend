package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmTheaterSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmTheaterSessionMapper {

    @Insert("INSERT INTO ilm_theater_sessions (user_id, tree_id, reality_positions, ideal_positions, " +
            "patterns, core_insight, action_commitment, radar_before, radar_after) " +
            "VALUES (#{userId}, #{treeId}, #{realityPositions}, #{idealPositions}, " +
            "#{patterns}, #{coreInsight}, #{actionCommitment}, #{radarBefore}, #{radarAfter})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmTheaterSession session);

    @Select("SELECT * FROM ilm_theater_sessions WHERE id = #{id} AND user_id = #{userId}")
    IlmTheaterSession selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM ilm_theater_sessions WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<IlmTheaterSession> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM ilm_theater_sessions WHERE user_id = #{userId} AND tree_id = #{treeId} ORDER BY create_time DESC")
    List<IlmTheaterSession> selectByUserIdAndTreeId(@Param("userId") Long userId, @Param("treeId") String treeId);

    @Delete("DELETE FROM ilm_theater_sessions WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
