package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmLegacyMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmLegacyMemberMapper {

    @Insert("INSERT INTO ilm_legacy_members (user_id, name, role, tone_style, atmosphere, catchphrases) " +
            "VALUES (#{userId}, #{name}, #{role}, #{toneStyle}, #{atmosphere}, #{catchphrases})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmLegacyMember member);

    @Select("SELECT * FROM ilm_legacy_members WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<IlmLegacyMember> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM ilm_legacy_members WHERE id = #{id} AND user_id = #{userId}")
    IlmLegacyMember selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM ilm_legacy_members WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ilm_legacy_members WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
