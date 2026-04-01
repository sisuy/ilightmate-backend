package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmPartner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IlmPartnerMapper {

    /** 通过推荐码查找伙伴 */
    @Select("SELECT * FROM ilm_partners WHERE referral_code = #{referralCode} AND status = '1'")
    IlmPartner selectByReferralCode(@Param("referralCode") String referralCode);

    /** 通过 user_id 查找导师伙伴记录 */
    @Select("SELECT * FROM ilm_partners WHERE user_id = #{userId} AND partner_type = 'mentor' AND status = '1'")
    IlmPartner selectMentorByUserId(@Param("userId") Long userId);

    /** 查找所有导师 */
    @Select("SELECT * FROM ilm_partners WHERE partner_type = 'mentor' AND status = '1' ORDER BY create_time DESC")
    List<IlmPartner> selectAllMentors();

    /** 查找所有合作伙伴（分页用，管理后台） */
    @Select("SELECT * FROM ilm_partners WHERE status = '1' ORDER BY create_time DESC")
    List<IlmPartner> selectAll();

    /** 按类型查找 */
    @Select("SELECT * FROM ilm_partners WHERE partner_type = #{type} AND status = '1' ORDER BY create_time DESC")
    List<IlmPartner> selectByType(@Param("type") String type);

    @Insert("INSERT INTO ilm_partners (partner_type, name, phone, user_id, referral_code, parent_id, sales_rep_id, status) " +
            "VALUES (#{partnerType}, #{name}, #{phone}, #{userId}, #{referralCode}, #{parentId}, #{salesRepId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(IlmPartner partner);

    @Update("UPDATE ilm_partners SET name = #{name}, phone = #{phone}, status = #{status}, " +
            "update_time = NOW() WHERE id = #{id}")
    int update(IlmPartner partner);

    @Select("SELECT * FROM ilm_partners WHERE id = #{id}")
    IlmPartner selectById(@Param("id") Long id);

    /** 检查推荐码是否已存在 */
    @Select("SELECT COUNT(*) > 0 FROM ilm_partners WHERE referral_code = #{referralCode}")
    boolean existsByReferralCode(@Param("referralCode") String referralCode);
}
