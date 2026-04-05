package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.RechargeCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 充值码 Mapper
 */
@Mapper
public interface RechargeCodeMapper extends BaseMapper<RechargeCodeEntity> {

    /**
     * 根据充值码查询
     */
    default RechargeCodeEntity selectByCode(String code) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RechargeCodeEntity>()
                .eq("code", code));
    }

    /**
     * 查询批次统计
     */
    List<java.util.Map<String, Object>> selectBatchStats(@Param("batchNo") String batchNo);
}
