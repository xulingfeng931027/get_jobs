package com.getjobs.application.service;

import com.getjobs.application.entity.ConsumptionLogEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.mapper.ConsumptionLogMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 计费服务 - 处理投递次数扣费和订阅状态检查
 */
@Slf4j
@Service
public class BillingService {

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private ConsumptionLogMapper consumptionLogMapper;

    /**
     * 投递前检查 - 验证用户是否有足够的余额或有效订阅
     *
     * @param userId 用户ID
     * @return 检查结果 {allowed: boolean, reason: String, applicationCount: int, hasSubscription: boolean}
     */
    public Map<String, Object> checkBeforeDelivery(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户余额
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null) {
            result.put("allowed", false);
            result.put("reason", "用户账户不存在，请先注册");
            result.put("applicationCount", 0);
            result.put("hasSubscription", false);
            return result;
        }

        int applicationCount = balance.getApplicationCount() != null ? balance.getApplicationCount() : 0;
        boolean hasSubscription = hasActiveSubscription(balance);

        result.put("applicationCount", applicationCount);
        result.put("hasSubscription", hasSubscription);
        result.put("subscriptionEndDate", balance.getSubscriptionEndDate());

        // 检查是否有足够的次数或有效订阅
        if (applicationCount > 0 || hasSubscription) {
            result.put("allowed", true);
            result.put("reason", "");
        } else {
            result.put("allowed", false);
            result.put("reason", "投递次数不足且无有效订阅，请充值后重试");
        }

        return result;
    }

    /**
     * 投递后扣费 - 扣减用户投递次数
     * 仅在余额充足或有效订阅时扣费
     *
     * @param userId 用户ID
     * @param count 投递数量
     * @param platform 招聘平台 (boss/liepin/job51/zhilian)
     * @return true=扣费成功, false=余额不足或无有效投递
     */
    @Transactional
    public boolean deductAfterDelivery(Long userId, int count, String platform) {
        // 无有效投递不扣费
        if (count <= 0) {
            log.info("[计费] 用户{}投递数量为0，不扣费", userId);
            return true;
        }

        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null) {
            log.warn("[计费] 用户{}不存在，跳过扣费", userId);
            return false;
        }

        // 有订阅则不扣费
        if (hasActiveSubscription(balance)) {
            log.info("[计费] 用户{}有有效订阅（到期：{}），不扣费，投递平台：{}",
                userId, balance.getSubscriptionEndDate(), platform);
            return true;
        }

        int currentCount = balance.getApplicationCount() != null ? balance.getApplicationCount() : 0;

        // 余额不足，不扣费
        if (currentCount < count) {
            log.warn("[计费] 用户{}余额不足（当前:{}，需要:{}），不扣费，投递平台：{}",
                userId, currentCount, count, platform);
            return false;
        }

        int balanceBefore = currentCount;
        int balanceAfter = currentCount - count;

        // 扣减次数
        userBalanceMapper.updateApplicationCount(userId, balanceAfter);

        // 记录消费日志
        ConsumptionLogEntity consumptionLog = new ConsumptionLogEntity();
        consumptionLog.setUserId(userId);
        consumptionLog.setType("delivery");
        consumptionLog.setAmount(count);
        consumptionLog.setPlatform(platform);
        consumptionLog.setBalanceBefore(balanceBefore);
        consumptionLog.setBalanceAfter(balanceAfter);
        consumptionLog.setCreatedAt(LocalDateTime.now());
        consumptionLogMapper.insert(consumptionLog);

        // 更新累计消费
        int totalConsumption = balance.getTotalConsumption() != null ? balance.getTotalConsumption() : 0;
        userBalanceMapper.updateTotalConsumption(userId, totalConsumption + count);

        log.info("[计费] 用户{}投递扣费成功：平台={}, 投递数={}, 扣费前={}, 扣费后={}",
            userId, platform, count, balanceBefore, balanceAfter);
        return true;
    }

    /**
     * 检查用户是否有有效订阅
     *
     * @param balance 用户余额实体
     * @return true=有有效订阅，false=无订阅或已过期
     */
    public boolean hasActiveSubscription(UserBalanceEntity balance) {
        if (balance.getSubscriptionEndDate() == null) {
            return false;
        }
        return balance.getSubscriptionEndDate().isAfter(LocalDateTime.now());
    }

    /**
     * 获取用户计费信息
     *
     * @param userId 用户ID
     * @return 计费信息 {applicationCount, hasSubscription, subscriptionEndDate, totalRecharge, totalConsumption}
     */
    public Map<String, Object> getUserBillingInfo(Long userId) {
        UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
        if (balance == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("applicationCount", balance.getApplicationCount() != null ? balance.getApplicationCount() : 0);
        result.put("hasSubscription", hasActiveSubscription(balance));
        result.put("subscriptionEndDate", balance.getSubscriptionEndDate());
        result.put("aiMatchCount", balance.getAiMatchCount() != null ? balance.getAiMatchCount() : 0);
        result.put("aiGreetCount", balance.getAiGreetCount() != null ? balance.getAiGreetCount() : 0);
        result.put("reportCount", balance.getReportCount() != null ? balance.getReportCount() : 0);
        result.put("totalRecharge", balance.getTotalRecharge() != null ? balance.getTotalRecharge() : 0);
        result.put("totalConsumption", balance.getTotalConsumption() != null ? balance.getTotalConsumption() : 0);

        return result;
    }
}
