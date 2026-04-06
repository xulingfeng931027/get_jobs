package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Boss直聘行业实体类
 */
@Data
@TableName("boss_industry")
public class BossIndustryEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 行业名称 */
    private String name;
    
    /** 行业代码 */
    private Integer code;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
