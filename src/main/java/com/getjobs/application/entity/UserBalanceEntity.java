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
    private Integer applicationCount;
    private Integer aiMatchCount;
    private Integer aiGreetCount;
    private Integer reportCount;
    private LocalDateTime subscriptionEndDate;
    private Integer totalRecharge;
    private Integer totalConsumption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
