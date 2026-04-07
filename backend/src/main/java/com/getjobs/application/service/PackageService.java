package com.getjobs.application.service;

import com.getjobs.application.entity.PackageDefinitionEntity;
import com.getjobs.application.entity.UserPackageEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.mapper.PackageDefinitionMapper;
import com.getjobs.application.mapper.UserPackageMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 套餐管理服务
 */
@Slf4j
@Service
public class PackageService {

    @Autowired
    private UserPackageMapper userPackageMapper;

    @Autowired
    private PackageDefinitionMapper packageDefinitionMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    /**
     * 开通套餐
     *
     * @param userId 用户ID
     * @param packageType 套餐类型 (1=周套餐, 2=月套餐)
     * @return 开通结果
     */
    @Transactional
    public Map<String, Object> activatePackage(Long userId, Integer packageType) {
        Map<String, Object> result = new HashMap<>();

        // 查询套餐定义
        PackageDefinitionEntity definition = packageDefinitionMapper.selectByType(packageType);
        if (definition == null) {
            result.put("success", false);
            result.put("message", "套餐类型不存在");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(definition.getDurationDays());

        // 检查用户是否已有有效套餐
        UserPackageEntity existingPackage = userPackageMapper.selectActivePackage(userId, now);
        if (existingPackage != null) {
            // 如果已有套餐，先计算剩余次数后创建新的
            int existingUsed = existingPackage.getUsedCount() != null ? existingPackage.getUsedCount() : 0;
            int existingTotal = existingPackage.getTotalCount() != null ? existingPackage.getTotalCount() : -1;
            int remaining = existingTotal == -1 ? -1 : existingTotal - existingUsed;

            // 将原套餐标记为已过期
            existingPackage.setStatus(3);
            userPackageMapper.updateById(existingPackage);

            result.put("previousPackageRemaining", remaining);
            result.put("previousPackageEndDate", existingPackage.getEndDate());
        }

        // 创建新套餐
        UserPackageEntity userPackage = new UserPackageEntity();
        userPackage.setUserId(userId);
        userPackage.setPackageType(packageType);
        userPackage.setStatus(1); // 激活
        userPackage.setTotalCount(definition.getMaxCount());
        userPackage.setUsedCount(0);
        userPackage.setStartDate(now);
        userPackage.setEndDate(endDate);
        userPackage.setCreateTime(now);
        userPackage.setUpdateTime(now);
        userPackageMapper.insert(userPackage);

        // 更新用户余额表中的套餐到期时间
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance != null && (balance.getSubscriptionEndDate() == null || balance.getSubscriptionEndDate().isBefore(endDate))) {
            userBalanceMapper.updateSubscriptionEndDate(userId, endDate);
        }

        log.info("[套餐] 用户{}开通套餐：类型={}, 到期时间={}", userId, packageType, endDate);

        result.put("success", true);
        result.put("packageId", userPackage.getId());
        result.put("packageType", packageType);
        result.put("packageName", definition.getName());
        result.put("startDate", now);
        result.put("endDate", endDate);
        result.put("totalCount", definition.getMaxCount());

        return result;
    }

    /**
     * 检查用户是否有有效套餐
     */
    public boolean hasActivePackage(Long userId) {
        UserPackageEntity pkg = userPackageMapper.selectActivePackage(userId, LocalDateTime.now());
        return pkg != null && pkg.isActive();
    }

    /**
     * 获取用户当前有效套餐
     */
    public UserPackageEntity getCurrentPackage(Long userId) {
        return userPackageMapper.selectActivePackage(userId, LocalDateTime.now());
    }

    /**
     * 检查用户是否还能投递（套餐有效且有剩余次数）
     */
    public boolean canDeliver(Long userId) {
        UserPackageEntity pkg = getCurrentPackage(userId);
        if (pkg == null || !pkg.isActive()) {
            return false;
        }
        return pkg.hasRemainingCount();
    }

    /**
     * 记录一次投递
     */
    @Transactional
    public boolean recordDelivery(Long userId) {
        UserPackageEntity pkg = getCurrentPackage(userId);
        if (pkg == null || !pkg.isActive()) {
            return false;
        }

        int usedCount = pkg.getUsedCount() != null ? pkg.getUsedCount() : 0;
        userPackageMapper.updateUsedCount(pkg.getId(), usedCount + 1);

        // 更新用户余额表的套餐已用次数
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance != null) {
            int packageUsedCount = balance.getPackageUsedCount() != null ? balance.getPackageUsedCount() : 0;
            userBalanceMapper.updatePackageUsedCount(userId, packageUsedCount + 1);
        }

        log.info("[套餐] 用户{}套餐投递记录：已用={}", userId, usedCount + 1);
        return true;
    }

    /**
     * 取消用户套餐
     */
    @Transactional
    public boolean cancelPackage(Long userId, Long packageId) {
        UserPackageEntity pkg = userPackageMapper.selectById(packageId);
        if (pkg == null || !pkg.getUserId().equals(userId)) {
            return false;
        }

        pkg.setStatus(0); // 未激活
        pkg.setUpdateTime(LocalDateTime.now());
        userPackageMapper.updateById(pkg);

        log.info("[套餐] 用户{}取消套餐：套餐ID={}", userId, packageId);
        return true;
    }

    /**
     * 获取用户套餐列表
     */
    public List<UserPackageEntity> getUserPackages(Long userId) {
        return userPackageMapper.selectByUserId(userId);
    }

    /**
     * 获取所有套餐类型
     */
    public List<PackageDefinitionEntity> getPackageDefinitions() {
        return packageDefinitionMapper.selectAllEnabled();
    }

    /**
     * 处理过期套餐（定时任务调用）
     */
    @Transactional
    public void processExpiredPackages() {
        LocalDateTime now = LocalDateTime.now();

        // 过期处理
        int expired = userPackageMapper.expirePackages(now);
        if (expired > 0) {
            log.info("[套餐] 过期处理：{}个套餐已过期", expired);
        }

        // 次数用完处理
        int usedUp = userPackageMapper.markUsedUpPackages();
        if (usedUp > 0) {
            log.info("[套餐] 次数用完处理：{}个套餐已用完", usedUp);
        }
    }

    /**
     * 获取用户套餐状态信息
     */
    public Map<String, Object> getPackageStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();

        UserPackageEntity pkg = getCurrentPackage(userId);
        if (pkg != null && pkg.isActive()) {
            result.put("hasActivePackage", true);
            result.put("packageType", pkg.getPackageType());
            result.put("totalCount", pkg.getTotalCount());
            result.put("usedCount", pkg.getUsedCount());
            result.put("remainingCount", pkg.getTotalCount() == -1 ? -1 : pkg.getTotalCount() - pkg.getUsedCount());
            result.put("startDate", pkg.getStartDate());
            result.put("endDate", pkg.getEndDate());

            PackageDefinitionEntity definition = packageDefinitionMapper.selectByType(pkg.getPackageType());
            result.put("packageName", definition != null ? definition.getName() : "未知套餐");
        } else {
            result.put("hasActivePackage", false);
        }

        return result;
    }
}
