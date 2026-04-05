package com.getjobs.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.getjobs.application.entity.RechargeLogEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.entity.UserEntity;
import com.getjobs.application.mapper.RechargeLogMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import com.getjobs.application.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理服务
 */
@Service
public class AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private RechargeLogMapper rechargeLogMapper;

    /**
     * 查询用户列表
     */
    public Map<String, Object> listUsers(int page, int size, Integer status, String keyword) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();

        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("email", keyword)
                    .or()
                    .like("phone", keyword));
        }
        queryWrapper.orderByDesc("created_at");

        Page<UserEntity> pageRequest = new Page<>(page, size);
        Page<UserEntity> pageResult = userMapper.selectPage(pageRequest, queryWrapper);

        // 为每个用户附加余额信息
        List<Map<String, Object>> userList = pageResult.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("phone", user.getPhone());
            map.put("status", user.getStatus());
            map.put("createdAt", user.getCreatedAt());

            UserBalanceEntity balance = userBalanceMapper.selectByUserId(user.getId());
            map.put("balance", balance != null ? balance.getApplicationCount() : 0);
            map.put("totalRecharge", balance != null ? balance.getTotalRecharge() : 0);
            map.put("totalConsumption", balance != null ? balance.getTotalConsumption() : 0);

            return map;
        }).toList();

        result.put("list", userList);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("totalPages", pageResult.getPages());

        return result;
    }

    /**
     * 查询用户详情
     */
    public Map<String, Object> getUserDetail(Long userId) {
        Map<String, Object> result = new HashMap<>();

        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("status", user.getStatus());
        result.put("createdAt", user.getCreatedAt());
        result.put("updatedAt", user.getUpdatedAt());

        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance != null) {
            result.put("balance", balance.getApplicationCount());
            result.put("totalRecharge", balance.getTotalRecharge());
            result.put("totalConsumption", balance.getTotalConsumption());
        } else {
            result.put("balance", 0);
            result.put("totalRecharge", 0);
            result.put("totalConsumption", 0);
        }

        return result;
    }

    /**
     * 禁用/启用用户
     */
    public boolean toggleUserStatus(Long userId, int status) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    /**
     * 查询用户的充值记录
     */
    public List<RechargeLogEntity> getUserRechargeLogs(Long userId) {
        return rechargeLogMapper.selectByUserId(userId);
    }
}
