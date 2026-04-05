package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.LicenseKeyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 客户端授权密钥 Mapper
 */
@Mapper
public interface LicenseKeyMapper extends BaseMapper<LicenseKeyEntity> {

    /**
     * 根据授权密钥查询
     */
    @Select("SELECT * FROM license_key WHERE license_key = #{licenseKey}")
    LicenseKeyEntity selectByLicenseKey(@Param("licenseKey") String licenseKey);

    /**
     * 更新授权状态
     */
    @Update("UPDATE license_key SET status = #{status}, activated_at = #{activatedAt}, expires_at = #{expiresAt}, device_fingerprint = #{deviceFingerprint} WHERE id = #{id}")
    int updateActivationStatus(@Param("id") Long id, 
                               @Param("status") int status, 
                               @Param("activatedAt") java.time.LocalDateTime activatedAt,
                               @Param("expiresAt") java.time.LocalDateTime expiresAt,
                               @Param("deviceFingerprint") String deviceFingerprint);

    /**
     * 更新状态
     */
    @Update("UPDATE license_key SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);
}
