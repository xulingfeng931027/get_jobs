package com.getjobs.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.entity.UserEntity;
import com.getjobs.application.mapper.RechargeCodeMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import com.getjobs.application.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台统计服务
 */
@Service
public class AdminStatsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private RechargeCodeMapper rechargeCodeMapper;

    /**
     * 获取仪表盘统计数据
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        QueryWrapper<UserEntity> activeUserQuery = new QueryWrapper<>();
        activeUserQuery.eq("status", 1);
        long activeUsers = userMapper.selectCount(activeUserQuery);

        // 充值码统计
        long totalCodes = rechargeCodeMapper.selectCount(null);
        QueryWrapper<com.getjobs.application.entity.RechargeCodeEntity> activatedQuery = new QueryWrapper<>();
        activatedQuery.eq("status", 1);
        long activatedCodes = rechargeCodeMapper.selectCount(activatedQuery);

        // 余额统计
        QueryWrapper<UserBalanceEntity> balanceQuery = new QueryWrapper<>();
        balanceQuery.select("SUM(balance) as total_balance, SUM(total_recharge) as total_recharge, SUM(total_consumption) as total_consumption");
        // 注意：MyBatis-Plus 的 selectCount 不支持 SUM，这里需要自定义查询
        // 暂时用简化版本
        result.put("totalUsers", totalUsers);
        result.put("activeUsers", activeUsers);
        result.put("totalCodes", totalCodes);
        result.put("activatedCodes", activatedCodes);
        result.put("totalBalance", 0); // 需要自定义 SQL
        result.put("totalRecharge", 0);
        result.put("totalConsumption", 0);

        return result;
    }

    /**
     * 获取用户增长趋势（最近 N 天）
     */
    public Map<String, Object> getUserTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        // 简化实现：返回最近 N 天的用户注册数
        // 实际应该按日期分组统计
        result.put("trend", new java.util.ArrayList<>());
        result.put("days", days);
        return result;
    }

    /**
     * 获取充值分布统计
     */
    public Map<String, Object> getRechargeDistribution() {
        Map<String, Object> result = new HashMap<>();
        // 简化实现
        result.put("distribution", new java.util.ArrayList<>());
        return result;
    }
}
