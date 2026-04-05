package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充值日志实体类
 */
@Data
@TableName("recharge_log")
public class RechargeLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String code;
    private Integer amount;
    private Integer bonus;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
