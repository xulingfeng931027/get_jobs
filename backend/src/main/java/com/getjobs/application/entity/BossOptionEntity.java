package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Boss直聘选项实体类
 */
@Data
@TableName("boss_option")
public class BossOptionEntity {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 选项类型: city-城市, industry-行业, experience-经验, jobType-职位类型, salary-薪资, degree-学历, scale-规模, stage-融资阶段 */
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
