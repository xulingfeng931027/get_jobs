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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 消费类型: application-投递, ai_match-AI匹配, ai_greet-AI打招呼, report-数据报告 */
    private String type;
    
    /** 消费数量 */
    private Integer amount;
    
    /** 平台名称: boss/liepin/51job/zhilian */
    private String platform;
    
    /** 岗位ID */
    private String jobId;
    
    /** 岗位名称 */
    private String jobName;
    
    /** 消费前余额 */
    private Integer balanceBefore;
    
    /** 消费后余额 */
    private Integer balanceAfter;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
