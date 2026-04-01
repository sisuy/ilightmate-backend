package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmUserSubscription;
import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmUserSubscriptionMapper {

    /** 获取用户当前活跃订阅（含试用 status='3'） */
    @Select("SELECT s.*, p.monthly_token_limit, p.daily_dialogue_limit " +
            "FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status IN ('1','3') AND s.end_time > NOW() " +
            "ORDER BY s.end_time DESC LIMIT 1")
    IlmUserSubscription selectActiveByUserId(@Param("userId") Long userId);

    /** 获取用户套餐的月 token 上限 */
    @Select("SELECT p.monthly_token_limit FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status IN ('1','3') AND s.end_time > NOW() " +
            "LIMIT 1")
    Integer selectTokenLimitByUserId(@Param("userId") Long userId);

    /** 获取用户套餐的日对话上限 */
    @Select("SELECT p.daily_dialogue_limit FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.user_id = #{userId} AND s.status IN ('1','3') AND s.end_time > NOW() " +
            "LIMIT 1")
    Integer selectDialogueLimitByUserId(@Param("userId") Long userId);

    /** 插入新订阅 */
    @Insert("INSERT INTO ilm_user_subscriptions (user_id, combo_id, combo_code, combo_name, combo_type, " +
            "daily_consult_limit, start_time, end_time, status) " +
            "VALUES (#{userId}, #{comboId}, #{comboCode}, #{comboName}, #{comboType}, " +
            "#{dailyConsultLimit}, #{startTime}, #{endTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmUserSubscription sub);

    /** 将用户所有活跃订阅标记为已过期（升级/续费时用） */
    @Update("UPDATE ilm_user_subscriptions SET status = '0', update_time = NOW() " +
            "WHERE user_id = #{userId} AND status IN ('1','3')")
    void expireByUserId(@Param("userId") Long userId);
}
