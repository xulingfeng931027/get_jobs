package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消费记录实体类
 */
@Data
@TableName("consumption_log")
public class ConsumptionLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private Integer amount;
    private String platform;
    private String jobId;
    private String jobName;
    private Integer balanceBefore;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
