package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmDialogueUsageMapper {

    @Insert("INSERT INTO ilm_dialogue_usage (user_id, date_key, count) VALUES (#{userId}, #{dateKey}, 1) " +
            "ON DUPLICATE KEY UPDATE count = count + 1, update_time = NOW()")
    void upsertCount(@Param("userId") Long userId, @Param("dateKey") String dateKey);

    @Select("SELECT count FROM ilm_dialogue_usage WHERE user_id = #{userId} AND date_key = #{dateKey}")
    Integer selectCount(@Param("userId") Long userId, @Param("dateKey") String dateKey);
}
