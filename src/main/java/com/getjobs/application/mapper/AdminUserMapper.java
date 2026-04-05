package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.AdminUserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理员 Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUserEntity> {

    /**
     * 根据用户名查询
     */
    default AdminUserEntity selectByUsername(String username) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AdminUserEntity>()
                .eq("username", username));
    }
}
