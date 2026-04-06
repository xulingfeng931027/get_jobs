package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.UserBalanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户余额 Mapper
 */
@Mapper
public interface UserBalanceMapper extends BaseMapper<UserBalanceEntity> {

    /**
     * 根据用户ID查询余额
     */
    @Select("SELECT * FROM user_balance WHERE user_id = #{userId} LIMIT 1")
    UserBalanceEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 更新投递次数
     */
    @Update("UPDATE user_balance SET application_count = #{count}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateApplicationCount(@Param("userId") Long userId, @Param("count") int count);

    /**
     * 更新累计消费
     */
    @Update("UPDATE user_balance SET total_consumption = #{total}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateTotalConsumption(@Param("userId") Long userId, @Param("total") int total);

    /**
     * 更新累计充值
     */
    @Update("UPDATE user_balance SET total_recharge = #{total}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateTotalRecharge(@Param("userId") Long userId, @Param("total") int total);

    /**
     * 更新订阅到期时间
     */
    @Update("UPDATE user_balance SET subscription_end_date = #{endDate}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateSubscriptionEndDate(@Param("userId") Long userId, @Param("endDate") java.time.LocalDateTime endDate);
}
