package com.getjobs.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户套餐实体类
 */
@Data
@TableName("user_package")
public class UserPackageEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 套餐类型: 1=周套餐, 2=月套餐 */
    private Integer packageType;

    /** 状态: 0=未激活, 1=激活, 2=已用完, 3=已过期 */
    private Integer status;

    /** 套餐总投递次数, -1=无限 */
    private Integer totalCount;

    /** 已使用次数 */
    private Integer usedCount;

    /** 套餐开始时间 */
    private LocalDateTime startDate;

    /** 套餐结束时间 */
    private LocalDateTime endDate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 检查套餐是否有效
     */
    public boolean isActive() {
        if (status == null || status != 1) {
            return false;
        }
        if (endDate != null && endDate.isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否还有剩余次数
     */
    public boolean hasRemainingCount() {
        if (totalCount == null || totalCount == -1) {
            return true; // 无限次数
        }
        return usedCount == null || usedCount < totalCount;
    }
}
