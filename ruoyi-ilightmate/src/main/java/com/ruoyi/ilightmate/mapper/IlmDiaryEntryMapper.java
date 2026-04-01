package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmDiaryEntry;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmDiaryEntryMapper {

    @Insert("INSERT INTO ilm_diary_entries (user_id, content, mood, emotions, family_mentions, ai_insight) " +
            "VALUES (#{userId}, #{content}, #{mood}, #{emotions}, #{familyMentions}, #{aiInsight})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmDiaryEntry entry);

    @Select("SELECT * FROM ilm_diary_entries WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<IlmDiaryEntry> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM ilm_diary_entries WHERE user_id = #{userId} " +
            "AND create_time BETWEEN #{startDate} AND #{endDate} ORDER BY create_time DESC")
    List<IlmDiaryEntry> selectByDateRange(@Param("userId") Long userId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    @Select("SELECT * FROM ilm_diary_entries WHERE id = #{id} AND user_id = #{userId}")
    IlmDiaryEntry selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM ilm_diary_entries WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ilm_diary_entries WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
