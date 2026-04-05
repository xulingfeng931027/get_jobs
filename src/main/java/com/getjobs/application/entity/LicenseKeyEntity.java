package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户端授权密钥实体类
 */
@Data
@TableName("license_key")
public class LicenseKeyEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String licenseKey;
    private Long userId;
    private String type;
    private Integer value;
    private Integer status;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;
    private String deviceFingerprint;
    private LocalDateTime createdAt;
}
