package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 猎聘岗位快照数据实体类
 * 保存从猎聘接口获取的有价值字段
 */
@Data
@TableName("liepin_data")
public class LiepinEntity {
    // ========== 岗位字段 ==========
    /** 主键ID/岗位ID */
    @TableId
    private Long jobId;
    
    /** 岗位名称 */
    private String jobTitle;
    
    /** 岗位链接 */
    private String jobLink;
    
    /** 薪资文本描述 */
    private String jobSalaryText;
    
    /** 工作地点 */
    private String jobArea;
    
    /** 学历要求 */
    private String jobEduReq;
    
    /** 经验要求 */
    private String jobExpReq;
    
    /** 刷新时间/发布时间 */
    private String jobPublishTime;

    // ========== 公司字段 ==========
    /** 公司ID */
    private Long compId;
    
    /** 公司名称 */
    private String compName;
    
    /** 公司行业 */
    private String compIndustry;
    
    /** 公司规模 */
    private String compScale;

    // ========== HR字段 ==========
    /** HR ID/招聘者ID */
    private String hrId;
    
    /** HR姓名 */
    private String hrName;
    
    /** HR职位 */
    private String hrTitle;
    
    /** HR即时通讯ID */
    private String hrImId;

    // ========== 投递状态 ==========
    /** 是否已投递: 0-未投递, 1-已投递 */
    private Integer delivered;

    // ========== 系统字段 ==========
    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}