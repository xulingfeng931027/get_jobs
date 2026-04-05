package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.UserDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户设备 Mapper
 */
@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDeviceEntity> {

    /**
     * 查询用户已绑定的设备数量
     */
    @Select("SELECT COUNT(*) FROM user_device WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的所有设备
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserDeviceEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询设备是否存在
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND device_fingerprint = #{fingerprint}")
    UserDeviceEntity selectByFingerprint(@Param("userId") Long userId, @Param("fingerprint") String fingerprint);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE user_device SET last_login_at = #{loginAt} WHERE id = #{id}")
    int updateLastLoginAt(@Param("id") Long id, @Param("loginAt") LocalDateTime loginAt);

    /**
     * 删除设备绑定
     */
    @Select("DELETE FROM user_device WHERE user_id = #{userId} AND device_fingerprint = #{fingerprint}")
    int deleteByFingerprint(@Param("userId") Long userId, @Param("fingerprint") String fingerprint);
}
