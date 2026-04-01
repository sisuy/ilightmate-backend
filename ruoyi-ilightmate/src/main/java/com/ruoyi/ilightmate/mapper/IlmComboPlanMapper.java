package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmComboPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmComboPlanMapper {

    @Select("SELECT * FROM ilm_combo_plans WHERE status = '1' ORDER BY order_num")
    List<IlmComboPlan> selectActivePlans();

    @Select("SELECT * FROM ilm_combo_plans WHERE combo_id = #{comboId}")
    IlmComboPlan selectById(@Param("comboId") Long comboId);

    @Select("SELECT monthly_token_limit FROM ilm_combo_plans WHERE combo_code = #{code} LIMIT 1")
    Integer selectTokenLimitByCode(@Param("code") String code);
}
