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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 充值码 */
    private String code;
    
    /** 类型: count-次数码, subscription-订阅码 */
    private String type;
    
    /** 投递次数(次数码) */
    private Integer applicationCount;
    
    /** 订阅天数(订阅码) */
    private Integer subscriptionDays;
    
    /** 面值/金额 */
    private Integer amount;
    
    /** 额外赠送 */
    private Integer bonus;
    
    /** 总价值 */
    private Integer totalValue;
    
    /** 状态: 0-未使用, 1-已激活, 2-已冻结, 3-已作废 */
    private Integer status;
    
    /** 批次号 */
    private String batchNo;
    
    /** 激活用户ID */
    private Long activatedBy;
    
    /** 激活时间 */
    private LocalDateTime activatedAt;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 创建人 */
    private String createdBy;
}
