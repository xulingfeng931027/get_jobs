package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Boss直聘岗位数据实体类
 */
@Data
@TableName("boss_data")
public class BossJobDataEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位加密ID */
    @TableField("encrypt_id")
    private String encryptId;

    /** 招聘者加密用户ID */
    @TableField("encrypt_user_id")
    private String encryptUserId;

    /** 公司名称 */
    @TableField("company_name")
    private String companyName;

    /** 岗位名称 */
    @TableField("job_name")
    private String jobName;

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

    /** HR姓名 */
    @TableField("hr_name")
    private String hrName;

    /** HR职位 */
    @TableField("hr_position")
    private String hrPosition;

    /** HR活跃状态 */
    @TableField("hr_active_status")
    private String hrActiveStatus;

    /** 投递状态: 未投递/已投递/已过滤/投递失败 */
    @TableField("delivery_status")
    private String deliveryStatus;

    /** 岗位描述 */
    @TableField("job_description")
    private String jobDescription;

    /** 岗位链接 */
    @TableField("job_url")
    private String jobUrl;

    /** 招聘状态 */
    @TableField("recruitment_status")
    private String recruitmentStatus;

    /** 公司地址 */
    @TableField("company_address")
    private String companyAddress;

    /** 所属行业 */
    @TableField("industry")
    private String industry;

    /** 公司介绍 */
    @TableField("introduce")
    private String introduce;

    /** 融资阶段 */
    @TableField("financing_stage")
    private String financingStage;

    /** 公司规模 */
    @TableField("company_scale")
    private String companyScale;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}