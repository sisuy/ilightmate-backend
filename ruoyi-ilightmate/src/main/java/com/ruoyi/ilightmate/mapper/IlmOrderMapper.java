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

    /** 检查用户是否曾经购买过某套餐（用于首月特惠判断） */
    @Select("SELECT COUNT(*) > 0 FROM ilm_orders o " +
            "JOIN ilm_combo_plans p ON o.combo_id = p.combo_id " +
            "WHERE o.user_id = #{userId} AND p.combo_code = #{comboCode} AND o.pay_status = 'PAID'")
    boolean hasEverPaid(@Param("userId") Long userId, @Param("comboCode") String comboCode);

    /** 获取用户最近一笔已支付的月付订单（用于自动续费查协议号） */
    @Select("SELECT o.* FROM ilm_orders o " +
            "JOIN ilm_combo_plans p ON o.combo_id = p.combo_id " +
            "WHERE o.user_id = #{userId} AND o.pay_status = 'PAID' AND p.billing_cycle = 'MONTHLY' " +
            "ORDER BY o.pay_time DESC LIMIT 1")
    IlmOrder selectLastPaidMonthlyByUserId(@Param("userId") Long userId);
}
