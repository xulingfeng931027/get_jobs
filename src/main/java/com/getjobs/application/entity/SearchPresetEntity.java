package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_preset")
public class SearchPresetEntity {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 预设名称 */
    private String name;
    /** 搜索关键词（逗号分隔文本） */
    private String keywords;
    /** 城市名称（跨平台统一存名称） */
    private String city;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
