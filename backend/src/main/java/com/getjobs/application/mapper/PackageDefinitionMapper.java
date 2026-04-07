package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.PackageDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐定义 Mapper
 */
@Mapper
public interface PackageDefinitionMapper extends BaseMapper<PackageDefinitionEntity> {

    /**
     * 查询所有启用的套餐类型
     */
    @Select("SELECT * FROM package_definition WHERE status = 1 ORDER BY type")
    List<PackageDefinitionEntity> selectAllEnabled();

    /**
     * 根据类型查询套餐定义
     */
    @Select("SELECT * FROM package_definition WHERE type = #{type} AND status = 1")
    PackageDefinitionEntity selectByType(Integer type);
}
