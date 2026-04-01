package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmCoachSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmCoachSessionMapper {

    @Insert("INSERT INTO ilm_coach_sessions (user_id, theme, sub_topic, current_step, sub_step, " +
            "analysis_data, insight_data, selected_companion, companion_dialogue_data, " +
            "action_commitment, growth_cycle, radar_before) " +
            "VALUES (#{userId}, #{theme}, #{subTopic}, #{currentStep}, #{subStep}, " +
            "#{analysisData}, #{insightData}, #{selectedCompanion}, #{companionDialogueData}, " +
            "#{actionCommitment}, #{growthCycle}, #{radarBefore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmCoachSession session);

    @Select("SELECT * FROM ilm_coach_sessions WHERE id = #{id} AND user_id = #{userId}")
    IlmCoachSession selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM ilm_coach_sessions WHERE user_id = #{userId} ORDER BY started_at DESC")
    List<IlmCoachSession> selectByUserId(@Param("userId") Long userId);

    /** 获取用户最近一个未完成的会话 */
    @Select("SELECT * FROM ilm_coach_sessions WHERE user_id = #{userId} AND completed = 0 " +
            "ORDER BY started_at DESC LIMIT 1")
    IlmCoachSession selectActiveByUserId(@Param("userId") Long userId);

    @Update("UPDATE ilm_coach_sessions SET current_step = #{currentStep}, sub_step = #{subStep}, " +
            "analysis_data = #{analysisData}, insight_data = #{insightData}, " +
            "selected_companion = #{selectedCompanion}, companion_dialogue_data = #{companionDialogueData}, " +
            "action_commitment = #{actionCommitment}, growth_cycle = #{growthCycle}, " +
            "radar_before = #{radarBefore}, completed = #{completed} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(IlmCoachSession session);

    @Delete("DELETE FROM ilm_coach_sessions WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
