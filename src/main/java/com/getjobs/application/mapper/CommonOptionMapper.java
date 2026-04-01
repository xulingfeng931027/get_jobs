package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.CommonOptionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公共选项Mapper
 */
@Mapper
public interface CommonOptionMapper extends BaseMapper<CommonOptionEntity> {

    /**
     * 按类型查询选项列表，按排序顺序升序
     */
    @Select("SELECT * FROM common_option WHERE type = #{type} ORDER BY sort_order ASC")
    List<CommonOptionEntity> selectByType(@Param("type") String type);
}
