package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.SubscriptionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订阅记录 Mapper
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<SubscriptionEntity> {

    /**
     * 查询用户当前有效订阅
     */
    @Select("SELECT * FROM subscription WHERE user_id = #{userId} AND status = 1 AND end_date > NOW() ORDER BY end_date DESC LIMIT 1")
    SubscriptionEntity selectActiveSubscription(@Param("userId") Long userId);

    /**
     * 查询用户订阅历史
     */
    @Select("SELECT * FROM subscription WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<SubscriptionEntity> selectUserHistory(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 更新订阅状态
     */
    @Update("UPDATE subscription SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 过期所有到期订阅
     */
    @Update("UPDATE subscription SET status = 0 WHERE end_date <= NOW() AND status = 1")
    int expireSubscriptions();
}
