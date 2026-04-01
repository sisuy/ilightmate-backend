package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmDialogueHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmDialogueHistoryMapper {

    @Insert("INSERT INTO ilm_dialogue_history (user_id, companion_id, conversation_summary, " +
            "key_discoveries, emotional_trajectory, commitments, message_count, duration) " +
            "VALUES (#{userId}, #{companionId}, #{conversationSummary}, " +
            "#{keyDiscoveries}, #{emotionalTrajectory}, #{commitments}, #{messageCount}, #{duration})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmDialogueHistory history);

    @Select("SELECT * FROM ilm_dialogue_history WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<IlmDialogueHistory> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM ilm_dialogue_history WHERE user_id = #{userId} AND companion_id = #{companionId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<IlmDialogueHistory> selectByUserAndCompanion(@Param("userId") Long userId,
                                                       @Param("companionId") String companionId,
                                                       @Param("limit") int limit);
}
