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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 充值码 */
    private String code;
    
    /** 充值数量 */
    private Integer amount;
    
    /** 额外赠送数量 */
    private Integer bonus;
    
    /** 充值前余额 */
    private Integer balanceBefore;
    
    /** 充值后余额 */
    private Integer balanceAfter;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
