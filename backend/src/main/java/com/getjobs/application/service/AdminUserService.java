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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
     * 查询用户列表（仅C端用户，排除admin_user表的管理员）
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

    /**
     * 创建C端用户
     */
    public Long createUser(String username, String email, String phone, String password) {
        // 检查用户名是否已存在
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户（密码使用BCrypt加密）
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 创建用户余额记录
        UserBalanceEntity balance = new UserBalanceEntity();
        balance.setUserId(user.getId());
        balance.setApplicationCount(0);
        balance.setAiMatchCount(0);
        balance.setAiGreetCount(0);
        balance.setReportCount(0);
        balance.setTotalRecharge(0);
        balance.setTotalConsumption(0);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());

        userBalanceMapper.insert(balance);

        return user.getId();
    }

    /**
     * 更新用户信息
     */
    public void updateUser(Long userId, String email, String phone, Integer status) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (status != null) {
            user.setStatus(status);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 删除用户余额记录
        QueryWrapper<UserBalanceEntity> balanceQuery = new QueryWrapper<>();
        balanceQuery.eq("user_id", userId);
        userBalanceMapper.delete(balanceQuery);

        // 删除用户
        userMapper.deleteById(userId);
    }
}
