package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智联招聘选项实体类
 */
@Data
@TableName("zhilian_option")
public class ZhilianOptionEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 选项类型: city-城市 等 */
    private String type;

    /** 选项名称 */
    private String name;

    /** 选项代码 */
    private String code;

    /** 显示排序（数值越小越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}