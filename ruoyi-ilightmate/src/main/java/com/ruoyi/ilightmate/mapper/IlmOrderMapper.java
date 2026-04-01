package com.ruoyi.ilightmate.mapper;

import com.ruoyi.ilightmate.domain.IlmOrder;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface IlmOrderMapper {

    @Insert("INSERT INTO ilm_orders (order_no, user_id, combo_id, pay_amount, order_status, pay_status, referral_code, region, remark) " +
            "VALUES (#{orderNo}, #{userId}, #{comboId}, #{payAmount}, #{orderStatus}, #{payStatus}, #{referralCode}, #{region}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    void insert(IlmOrder order);

    @Select("SELECT * FROM ilm_orders WHERE order_id = #{orderId}")
    IlmOrder selectById(@Param("orderId") Long orderId);

    @Select("SELECT * FROM ilm_orders WHERE order_no = #{orderNo}")
    IlmOrder selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM ilm_orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<IlmOrder> selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE ilm_orders SET pay_status = #{payStatus}, pay_type = #{payType}, " +
            "transaction_no = #{transactionNo}, pay_time = #{payTime}, order_status = 'PAID', update_time = NOW() " +
            "WHERE order_no = #{orderNo}")
    void updatePayStatus(@Param("orderNo") String orderNo, @Param("payStatus") String payStatus,
                         @Param("payType") String payType, @Param("transactionNo") String transactionNo,
                         @Param("payTime") Date payTime);
}
