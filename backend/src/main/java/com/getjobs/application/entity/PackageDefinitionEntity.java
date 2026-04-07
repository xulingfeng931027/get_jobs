package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 套餐类型定义实体类
 */
@Data
@TableName("package_definition")
public class PackageDefinitionEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐类型: 1=周套餐, 2=月套餐 */
    private Integer type;

    /** 套餐名称 */
    private String name;

    /** 时长(天) */
    private Integer durationDays;

    /** 最大投递次数, -1=无限 */
    private Integer maxCount;

    /** 描述 */
    private String description;

    /** 状态: 0=禁用, 1=启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
