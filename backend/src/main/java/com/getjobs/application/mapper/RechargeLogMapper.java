package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.RechargeLogEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 充值日志 Mapper
 */
@Mapper
public interface RechargeLogMapper extends BaseMapper<RechargeLogEntity> {

    /**
     * 查询用户的充值记录
     */
    default List<RechargeLogEntity> selectByUserId(Long userId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RechargeLogEntity>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
    }
}
