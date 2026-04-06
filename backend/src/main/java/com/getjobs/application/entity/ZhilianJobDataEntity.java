package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智联招聘岗位数据实体类
 */
@Data
@TableName("zhilian_data")
public class ZhilianJobDataEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位ID */
    @TableField("job_id")
    private String jobId;

    /** 岗位名称 */
    @TableField("job_title")
    private String jobTitle;

    /** 岗位链接 */
    @TableField("job_link")
    private String jobLink;

    /** 薪资范围 */
    @TableField("salary")
    private String salary;

    /** 工作地点 */
    @TableField("location")
    private String location;

    /** 经验要求 */
    @TableField("experience")
    private String experience;

    /** 学历要求 */
    @TableField("degree")
    private String degree;

    /** 公司名称 */
    @TableField("company_name")
    private String companyName;

    /** 投递状态: 未投递/已投递/已过滤/投递失败 */
    @TableField("delivery_status")
    private String deliveryStatus;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}