package com.ruoyi.ilightmate.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * ilm_activities - activity feed / in-app notification storage
 */
@Mapper
public interface IlmActivityMapper {

    @Insert("INSERT INTO ilm_activities (user_id, type, action, detail) VALUES (#{userId}, #{type}, #{action}, #{detail})")
    void insert(@Param("userId") Long userId, @Param("type") String type,
                @Param("action") String action, @Param("detail") String detail);

    @Select("SELECT * FROM ilm_activities WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
