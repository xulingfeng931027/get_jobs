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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 授权密钥 */
    private String licenseKey;
    
    /** 用户ID */
    private Long userId;
    
    /** 类型: count-次数, subscription-订阅 */
    private String type;
    
    /** 值(次数或天数) */
    private Integer value;
    
    /** 状态: 0-未激活, 1-已激活, 2-已过期, 3-已作废 */
    private Integer status;
    
    /** 激活时间 */
    private LocalDateTime activatedAt;
    
    /** 过期时间 */
    private LocalDateTime expiresAt;
    
    /** 设备指纹 */
    private String deviceFingerprint;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
