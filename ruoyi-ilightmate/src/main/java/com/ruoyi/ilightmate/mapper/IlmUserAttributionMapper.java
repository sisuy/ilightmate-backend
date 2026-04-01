package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmUserAttributionMapper {

    @Select("SELECT locked FROM ilm_user_attribution WHERE user_id = #{userId}")
    Boolean isLocked(@Param("userId") Long userId);

    @Insert("INSERT INTO ilm_user_attribution (user_id, entry_type, referral_code) " +
            "VALUES (#{userId}, 'channel', #{referralCode}) " +
            "ON DUPLICATE KEY UPDATE referral_code = #{referralCode}, update_time = NOW()")
    void upsertAttribution(@Param("userId") Long userId, @Param("referralCode") String referralCode);

    @Update("UPDATE ilm_user_attribution SET locked = 1, locked_at = NOW() WHERE user_id = #{userId}")
    void lockAttribution(@Param("userId") Long userId);
}
