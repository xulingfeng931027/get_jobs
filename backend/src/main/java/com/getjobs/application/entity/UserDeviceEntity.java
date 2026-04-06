package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设备绑定实体类
 */
@Data
@TableName("user_device")
public class UserDeviceEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 设备指纹 */
    private String deviceFingerprint;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
