package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类（商业化系统）
 */
@Data
@TableName("user")
public class UserEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 邮箱地址 */
    private String email;
    
    /** 手机号码 */
    private String phone;
    
    /** 密码哈希值 */
    private String passwordHash;
    
    /** 状态: 0-禁用, 1-启用 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
