package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递报告实体
 * 记录客户端上报的投递结果
 */
@Data
@TableName("delivery_report")
public class DeliveryReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报告ID (rpt_xxx) */
    private String reportId;

    /** 用户ID */
    private Long userId;

    /** 平台 (boss/liepin/job51/zhilian) */
    private String platform;

    /** 成功投递数 */
    private Integer deliveredCount;

    /** 被过滤数 */
    private Integer filteredCount;

    /** 失败数 */
    private Integer failedCount;

    /** 总计处理数 */
    private Integer totalCount;

    /** 成功投递的职位详情 (JSON) */
    private String deliveredJobs;

    /** 被过滤的职位详情 (JSON) */
    private String filteredJobs;

    /** 失败的职位详情 (JSON) */
    private String failedJobs;

    /** 设备ID */
    private String deviceId;

    /** 客户端版本 */
    private String clientVersion;

    /** 创建时间 */
    private LocalDateTime createdAt;
}