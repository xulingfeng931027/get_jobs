package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.ConsumptionLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消费记录 Mapper
 */
@Mapper
public interface ConsumptionLogMapper extends BaseMapper<ConsumptionLogEntity> {
    
    /**
     * 根据用户ID查询消费记录
     */
    default List<ConsumptionLogEntity> selectByUserId(@Param("userId") Long userId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ConsumptionLogEntity>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
    }
}
