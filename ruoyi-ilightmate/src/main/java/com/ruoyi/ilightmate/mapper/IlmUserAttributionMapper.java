package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface IlmUserAttributionMapper {

    /** 检查归因是否已锁定 */
    @Select("SELECT locked FROM ilm_user_attribution WHERE user_id = #{userId}")
    Boolean isLocked(@Param("userId") Long userId);

    /** 获取用户的入口类型 */
    @Select("SELECT entry_type FROM ilm_user_attribution WHERE user_id = #{userId}")
    String getEntryType(@Param("userId") Long userId);

    /** 获取推荐该用户的导师的 user_id（通过 partner → sys_user 关联） */
    @Select("SELECT p.user_id FROM ilm_user_attribution a " +
            "JOIN ilm_partners p ON a.referral_code = p.referral_code " +
            "WHERE a.user_id = #{userId} AND a.entry_type = 'mentor_referral' " +
            "LIMIT 1")
    Long getMentorUserId(@Param("userId") Long userId);

    /** 创建或更新归因（绑定渠道/直销推荐码） */
    @Insert("INSERT INTO ilm_user_attribution (user_id, entry_type, referral_code) " +
            "VALUES (#{userId}, 'channel', #{referralCode}) " +
            "ON DUPLICATE KEY UPDATE referral_code = #{referralCode}, update_time = NOW()")
    void upsertAttribution(@Param("userId") Long userId, @Param("referralCode") String referralCode);

    /** 创建或更新归因（绑定导师 MT 码） */
    @Insert("INSERT INTO ilm_user_attribution (user_id, entry_type, referral_code, referrer_name) " +
            "VALUES (#{userId}, 'mentor_referral', #{referralCode}, #{referrerName}) " +
            "ON DUPLICATE KEY UPDATE entry_type = 'mentor_referral', referral_code = #{referralCode}, " +
            "referrer_name = #{referrerName}, update_time = NOW()")
    void upsertMentorAttribution(@Param("userId") Long userId,
                                  @Param("referralCode") String referralCode,
                                  @Param("referrerName") String referrerName);

    /** 首次付费后锁定归因 */
    @Update("UPDATE ilm_user_attribution SET locked = 1, locked_at = NOW(), update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void lockAttribution(@Param("userId") Long userId);

    /** 查找某推荐码下的所有客户（导师客户列表） */
    @Select("SELECT a.user_id, u.nick_name, u.phonenumber, u.create_time AS registered_at, " +
            "a.locked, a.locked_at, " +
            "CASE WHEN EXISTS (SELECT 1 FROM ilm_orders o WHERE o.user_id = a.user_id AND o.pay_status = 'PAID') " +
            "THEN 1 ELSE 0 END AS has_paid " +
            "FROM ilm_user_attribution a " +
            "JOIN sys_user u ON a.user_id = u.user_id " +
            "WHERE a.referral_code = #{referralCode} " +
            "ORDER BY u.create_time DESC")
    List<Map<String, Object>> selectClientsByReferralCode(@Param("referralCode") String referralCode);

    /** 统计推荐码下的总客户数 */
    @Select("SELECT COUNT(*) FROM ilm_user_attribution WHERE referral_code = #{referralCode}")
    int countByReferralCode(@Param("referralCode") String referralCode);

    /** 统计推荐码下的已付费客户数 */
    @Select("SELECT COUNT(DISTINCT a.user_id) FROM ilm_user_attribution a " +
            "JOIN ilm_orders o ON a.user_id = o.user_id " +
            "WHERE a.referral_code = #{referralCode} AND o.pay_status = 'PAID'")
    int countPaidByReferralCode(@Param("referralCode") String referralCode);

    /** 获取用户的归因信息 */
    @Select("SELECT * FROM ilm_user_attribution WHERE user_id = #{userId}")
    Map<String, Object> selectByUserId(@Param("userId") Long userId);
}
