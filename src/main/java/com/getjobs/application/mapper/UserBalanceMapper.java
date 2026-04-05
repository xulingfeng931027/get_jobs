package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.UserBalanceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户余额 Mapper
 */
@Mapper
public interface UserBalanceMapper extends BaseMapper<UserBalanceEntity> {

    /**
     * 根据用户 ID 查询余额
     */
    default UserBalanceEntity selectByUserId(Long userId) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserBalanceEntity>()
                .eq("user_id", userId));
    }
}
