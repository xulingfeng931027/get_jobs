package com.getjobs.application.service;

import com.getjobs.application.entity.SubscriptionEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.mapper.SubscriptionMapper;
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
 * 订阅管理服务
 */
@Slf4j
@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionMapper subscriptionMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    /**
     * 激活订阅
     *
     * @param userId 用户ID
     * @param type 订阅类型 (weekly/monthly/quarterly)
     * @param days 订阅天数
     * @param rechargeCodeId 关联的充值码ID
     * @return 订阅信息
     */
    @Transactional
    public Map<String, Object> activateSubscription(Long userId, String type, int days, Long rechargeCodeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);

        // 创建订阅记录
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setUserId(userId);
        subscription.setType(type);
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setStatus(1);
        subscription.setRechargeCodeId(rechargeCodeId);
        subscription.setCreatedAt(now);
        subscriptionMapper.insert(subscription);

        // 更新用户余额表中的订阅到期时间
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null) {
            // 如果余额记录不存在，创建一条
            balance = new UserBalanceEntity();
            balance.setUserId(userId);
            balance.setApplicationCount(0);
            balance.setAiMatchCount(0);
            balance.setAiGreetCount(0);
            balance.setReportCount(0);
            balance.setTotalRecharge(0);
            balance.setTotalConsumption(0);
            balance.setCreatedAt(now);
            balance.setUpdatedAt(now);
            userBalanceMapper.insert(balance);
        }

        // 如果当前订阅到期时间晚于新订阅到期时间，则不更新
        if (balance.getSubscriptionEndDate() != null && balance.getSubscriptionEndDate().isAfter(endDate)) {
            log.info("[订阅] 用户{}已有更长的订阅有效期，不更新", userId);
        } else {
            userBalanceMapper.updateSubscriptionEndDate(userId, endDate);
            log.info("[订阅] 用户{}激活订阅：类型={}, 到期时间={}", userId, type, endDate);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("type", type);
        result.put("startDate", now);
        result.put("endDate", endDate);
        result.put("days", days);

        return result;
    }

    /**
     * 查询用户订阅状态
     *
     * @param userId 用户ID
     * @return 订阅状态信息
     */
    public Map<String, Object> getSubscriptionStatus(Long userId) {
        SubscriptionEntity activeSubscription = subscriptionMapper.selectActiveSubscription(userId);
        
        Map<String, Object> result = new HashMap<>();
        if (activeSubscription != null) {
            result.put("hasSubscription", true);
            result.put("type", activeSubscription.getType());
            result.put("startDate", activeSubscription.getStartDate());
            result.put("endDate", activeSubscription.getEndDate());
            result.put("status", "active");
            
            // 计算剩余天数
            LocalDateTime now = LocalDateTime.now();
            long remainingDays = java.time.Duration.between(now, activeSubscription.getEndDate()).toDays();
            result.put("remainingDays", remainingDays);
        } else {
            result.put("hasSubscription", false);
            result.put("status", "none");
        }

        return result;
    }

    /**
     * 查询用户订阅历史
     *
     * @param userId 用户ID
     * @param limit 查询条数
     * @return 订阅历史列表
     */
    public List<SubscriptionEntity> getSubscriptionHistory(Long userId, int limit) {
        return subscriptionMapper.selectUserHistory(userId, limit);
    }

    /**
     * 检查用户是否有有效订阅
     *
     * @param userId 用户ID
     * @return true=有有效订阅
     */
    public boolean hasActiveSubscription(Long userId) {
        SubscriptionEntity subscription = subscriptionMapper.selectActiveSubscription(userId);
        return subscription != null;
    }

    /**
     * 取消订阅
     *
     * @param userId 用户ID
     * @param subscriptionId 订阅ID
     * @return 是否成功
     */
    @Transactional
    public boolean cancelSubscription(Long userId, Long subscriptionId) {
        SubscriptionEntity subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null || !subscription.getUserId().equals(userId)) {
            return false;
        }

        subscriptionMapper.updateStatus(subscriptionId, 2);
        log.info("[订阅] 用户{}取消订阅{}", userId, subscriptionId);
        return true;
    }
}
