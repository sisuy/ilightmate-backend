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

    /** 定时任务：将所有已过期的订阅标记为 status='0' */
    @Update("UPDATE ilm_user_subscriptions SET status = '0', update_time = NOW() " +
            "WHERE end_time < NOW() AND status IN ('1','3')")
    int expireOverdue();

    /** 查找 N 天内到期的活跃订阅（续费提醒用） */
    @Select("SELECT * FROM ilm_user_subscriptions " +
            "WHERE status IN ('1','3') " +
            "AND end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY end_time ASC")
    List<IlmUserSubscription> selectExpiringSoon(@Param("days") int days);

    /** 查找最近 N 天内刚过期的订阅（挽回用） */
    @Select("SELECT * FROM ilm_user_subscriptions " +
            "WHERE status = '0' " +
            "AND end_time BETWEEN DATE_SUB(NOW(), INTERVAL #{days} DAY) AND NOW() " +
            "ORDER BY end_time DESC")
    List<IlmUserSubscription> selectRecentlyExpired(@Param("days") int days);

    /** 查找 N 天内到期的月付订阅（自动续费用） */
    @Select("SELECT s.* FROM ilm_user_subscriptions s " +
            "JOIN ilm_combo_plans p ON s.combo_id = p.combo_id " +
            "WHERE s.status = '1' AND p.billing_cycle = 'MONTHLY' " +
            "AND s.end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY s.end_time ASC")
    List<IlmUserSubscription> selectMonthlyExpiringSoon(@Param("days") int days);
}
