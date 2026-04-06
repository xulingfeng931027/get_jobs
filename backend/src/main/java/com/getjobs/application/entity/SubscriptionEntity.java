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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 订阅类型: subscription-订阅 */
    private String type;
    
    /** 订阅开始时间 */
    private LocalDateTime startDate;
    
    /** 订阅结束时间 */
    private LocalDateTime endDate;
    
    /** 状态: 0-未激活, 1-有效, 2-已过期, 3-已取消 */
    private Integer status;
    
    /** 关联充值码ID */
    private Long rechargeCodeId;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
