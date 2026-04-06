package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.SearchPresetEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索预设Mapper
 */
@Mapper
public interface SearchPresetMapper extends BaseMapper<SearchPresetEntity> {
}
