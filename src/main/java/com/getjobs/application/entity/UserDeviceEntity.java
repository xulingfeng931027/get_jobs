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
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String deviceFingerprint;
    private String deviceName;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
