package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("common_option")
public class CommonOptionEntity {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 选项类型 */
    private String type;
    /** 显示标签 */
    private String label;
    /** 选项值 */
    private String value;
    /** 排序顺序 */
    private Integer sortOrder;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
