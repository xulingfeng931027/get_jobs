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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 剩余投递次数 */
    private Integer applicationCount;
    
    /** AI匹配次数 */
    private Integer aiMatchCount;
    
    /** AI打招呼次数 */
    private Integer aiGreetCount;
    
    /** 数据报告次数 */
    private Integer reportCount;
    
    /** 订阅结束时间 */
    private LocalDateTime subscriptionEndDate;
    
    /** 累计充值次数 */
    private Integer totalRecharge;
    
    /** 累计消费次数 */
    private Integer totalConsumption;

    /** 套餐已用投递次数 */
    private Integer packageUsedCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
