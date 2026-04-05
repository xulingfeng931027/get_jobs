package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 充值码实体类
 */
@Data
@TableName("recharge_code")
public class RechargeCodeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Integer amount;
    private Integer bonus;
    private Integer totalValue;
    private Integer status;
    private String batchNo;
    private Long activatedBy;
    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;
    private String createdBy;
}
