package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订阅记录实体类
 */
@Data
@TableName("subscription")
public class SubscriptionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;
    private Long rechargeCodeId;
    private LocalDateTime createdAt;
}
