package com.getjobs.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.getjobs.application.entity.UserPackageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户套餐 Mapper
 */
@Mapper
public interface UserPackageMapper extends BaseMapper<UserPackageEntity> {

    /**
     * 查询用户当前有效套餐
     */
    @Select("SELECT * FROM user_package WHERE user_id = #{userId} AND status = 1 AND end_date > #{now} ORDER BY end_date DESC LIMIT 1")
    UserPackageEntity selectActivePackage(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 查询用户所有套餐
     */
    @Select("SELECT * FROM user_package WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserPackageEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新使用次数
     */
    @Update("UPDATE user_package SET used_count = #{usedCount}, update_time = NOW() WHERE id = #{id}")
    int updateUsedCount(@Param("id") Long id, @Param("usedCount") Integer usedCount);

    /**
     * 过期所有到期套餐
     */
    @Update("UPDATE user_package SET status = 3 WHERE end_date <= #{now} AND status = 1")
    int expirePackages(@Param("now") LocalDateTime now);

    /**
     * 将次数用完的套餐标记为已用完
     */
    @Update("UPDATE user_package SET status = 2 WHERE status = 1 AND total_count != -1 AND used_count >= total_count")
    int markUsedUpPackages();
}
