package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmExploreScore;
import org.apache.ibatis.annotations.*;

@Mapper
public interface IlmExploreScoreMapper {

    @Select("SELECT * FROM ilm_explore_scores WHERE user_id = #{userId}")
    IlmExploreScore selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO ilm_explore_scores (user_id, dim_andun, dim_lijie, dim_qingxu, dim_lianjie, " +
            "dim_xingdong, dim_jiena, dim_juecha) " +
            "VALUES (#{userId}, #{dimAndun}, #{dimLijie}, #{dimQingxu}, #{dimLianjie}, " +
            "#{dimXingdong}, #{dimJiena}, #{dimJuecha})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmExploreScore score);

    @Update("UPDATE ilm_explore_scores SET dim_andun = #{dimAndun}, dim_lijie = #{dimLijie}, " +
            "dim_qingxu = #{dimQingxu}, dim_lianjie = #{dimLianjie}, dim_xingdong = #{dimXingdong}, " +
            "dim_jiena = #{dimJiena}, dim_juecha = #{dimJuecha}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateByUserId(IlmExploreScore score);
}
