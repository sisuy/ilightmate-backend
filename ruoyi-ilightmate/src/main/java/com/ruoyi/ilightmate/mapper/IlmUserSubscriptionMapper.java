package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmUserSubscriptionMapper {

    @Select("SELECT s.*, p.monthly_token_limit, p.daily_dialogue_limit " +
            "FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status = '1' AND s.end_time > NOW() " +
            "ORDER BY s.end_time DESC LIMIT 1")
    IlmUserSubscription selectActiveByUserId(@Param("userId") Long userId);

    @Select("SELECT p.monthly_token_limit FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status = '1' AND s.end_time > NOW() " +
            "LIMIT 1")
    Integer selectTokenLimitByUserId(@Param("userId") Long userId);

    @Select("SELECT p.daily_dialogue_limit FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status = '1' AND s.end_time > NOW() " +
            "LIMIT 1")
    Integer selectDialogueLimitByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO ilm_user_subscriptions (user_id, combo_id, combo_code, combo_name, combo_type, daily_consult_limit, start_time, end_time) " +
            "VALUES (#{userId}, #{comboId}, #{comboCode}, #{comboName}, #{comboType}, #{dailyConsultLimit}, #{startTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmUserSubscription sub);
}
