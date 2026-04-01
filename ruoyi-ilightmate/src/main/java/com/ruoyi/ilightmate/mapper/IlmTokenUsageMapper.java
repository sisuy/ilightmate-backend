package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmTokenUsage;
import org.apache.ibatis.annotations.*;

/**
 * Token 使用量 Mapper
 */
@Mapper
public interface IlmTokenUsageMapper {

    @Select("SELECT * FROM ilm_token_usage WHERE user_id = #{userId} AND month_key = #{monthKey}")
    IlmTokenUsage selectByUserAndMonth(@Param("userId") Long userId, @Param("monthKey") String monthKey);

    @Insert("INSERT INTO ilm_token_usage (user_id, month_key, used, bonus) VALUES (#{userId}, #{monthKey}, #{increment}, 0) " +
            "ON DUPLICATE KEY UPDATE used = used + #{increment}, update_time = NOW()")
    void upsertUsage(@Param("userId") Long userId, @Param("monthKey") String monthKey, @Param("increment") int increment);

    @Update("INSERT INTO ilm_token_usage (user_id, month_key, used, bonus) VALUES (#{userId}, #{monthKey}, 0, #{bonus}) " +
            "ON DUPLICATE KEY UPDATE bonus = bonus + #{bonus}, update_time = NOW()")
    void addBonus(@Param("userId") Long userId, @Param("monthKey") String monthKey, @Param("bonus") int bonus);
}
