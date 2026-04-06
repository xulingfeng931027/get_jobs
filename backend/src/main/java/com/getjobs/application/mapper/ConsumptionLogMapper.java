package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.ConsumptionLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消费记录 Mapper
 */
@Mapper
public interface ConsumptionLogMapper extends BaseMapper<ConsumptionLogEntity> {
}
