package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户余额实体类
 */
@Data
@TableName("user_balance")
public class UserBalanceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalRecharge;
    private Integer totalConsumption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
