package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;

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

    /** 创建或更新归因（绑定推荐码） */
    @Insert("INSERT INTO ilm_user_attribution (user_id, entry_type, referral_code) " +
            "VALUES (#{userId}, 'channel', #{referralCode}) " +
            "ON DUPLICATE KEY UPDATE referral_code = #{referralCode}, update_time = NOW()")
    void upsertAttribution(@Param("userId") Long userId, @Param("referralCode") String referralCode);

    /** 首次付费后锁定归因 */
    @Update("UPDATE ilm_user_attribution SET locked = 1, locked_at = NOW(), update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void lockAttribution(@Param("userId") Long userId);
}
