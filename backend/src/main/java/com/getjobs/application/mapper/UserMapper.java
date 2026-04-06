package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询
     */
    default UserEntity selectByUsername(String username) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserEntity>()
                .eq("username", username));
    }

    /**
     * 根据邮箱查询
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询
     */
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    UserEntity selectByPhone(@Param("phone") String phone);
}
