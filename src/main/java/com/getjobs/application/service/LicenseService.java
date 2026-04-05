package com.getjobs.application.service;

import com.getjobs.application.entity.LicenseKeyEntity;
import com.getjobs.application.entity.UserBalanceEntity;
import com.getjobs.application.mapper.LicenseKeyMapper;
import com.getjobs.application.mapper.UserBalanceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端授权密钥管理服务
 */
@Slf4j
@Service
public class LicenseService {

    /**
     * 授权密钥字符集（排除易混淆字符：0O1lI）
     */
    private static final String CHAR_SET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    @Autowired
    private LicenseKeyMapper licenseKeyMapper;
    @Autowired
    private UserBalanceMapper userBalanceMapper;

    /**
     * 生成授权密钥
     *
     * @param userId 用户ID
     * @param type 授权类型 (count/subscription)
     * @param value 授权值（次数或天数）
     * @return 授权密钥
     */
    @Transactional
    public String generateLicenseKey(Long userId, String type, int value) {
        String licenseKey = "GJ-" + type.toUpperCase().substring(0, 3) + "-" + generateRandomCode(16);

        LicenseKeyEntity entity = new LicenseKeyEntity();
        entity.setLicenseKey(licenseKey);
        entity.setUserId(userId);
        entity.setType(type);
        entity.setValue(value);
        entity.setStatus(1);
        entity.setCreatedAt(LocalDateTime.now());
        licenseKeyMapper.insert(entity);

        log.info("[授权密钥] 生成密钥：用户={}, 类型={}, 值={}", userId, type, value);
        return licenseKey;
    }

    /**
     * 激活授权密钥
     *
     * @param userId 用户ID
     * @param licenseKey 授权密钥
     * @param deviceFingerprint 设备指纹
     * @return 激活结果
     */
    @Transactional
    public Map<String, Object> activateLicenseKey(Long userId, String licenseKey, String deviceFingerprint) {
        Map<String, Object> result = new HashMap<>();

        LicenseKeyEntity entity = licenseKeyMapper.selectByLicenseKey(licenseKey);
        if (entity == null) {
            result.put("success", false);
            result.put("message", "授权密钥不存在");
            return result;
        }

        if (entity.getStatus() != 1) {
            result.put("success", false);
            result.put("message", "授权密钥已使用或已冻结");
            return result;
        }

        if (!entity.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "授权密钥不属于当前用户");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = null;

        // 根据类型处理授权
        if ("count".equals(entity.getType())) {
            // 次数授权：增加用户投递次数
            UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
            int currentCount = balance != null && balance.getApplicationCount() != null ? balance.getApplicationCount() : 0;
            int newCount = currentCount + entity.getValue();
            
            if (balance == null) {
                balance = new UserBalanceEntity();
                balance.setUserId(userId);
                balance.setApplicationCount(newCount);
                balance.setAiMatchCount(0);
                balance.setAiGreetCount(0);
                balance.setReportCount(0);
                balance.setTotalRecharge(0);
                balance.setTotalConsumption(0);
                balance.setCreatedAt(now);
                userBalanceMapper.insert(balance);
            } else {
                userBalanceMapper.updateApplicationCount(userId, newCount);
            }

            log.info("[授权密钥] 用户{}激活次数授权：{}次，当前余额：{}", userId, entity.getValue(), newCount);
        } else if ("subscription".equals(entity.getType())) {
            // 订阅授权：设置订阅到期时间
            expiresAt = now.plusDays(entity.getValue());
            UserBalanceEntity balance = userBalanceMapper.selectByUserId(userId);
            
            if (balance == null) {
                balance = new UserBalanceEntity();
                balance.setUserId(userId);
                balance.setApplicationCount(0);
                balance.setAiMatchCount(0);
                balance.setAiGreetCount(0);
                balance.setReportCount(0);
                balance.setTotalRecharge(0);
                balance.setTotalConsumption(0);
                balance.setSubscriptionEndDate(expiresAt);
                balance.setCreatedAt(now);
                userBalanceMapper.insert(balance);
            } else {
                // 如果当前订阅到期时间晚于新授权，则不更新
                if (balance.getSubscriptionEndDate() != null && balance.getSubscriptionEndDate().isAfter(expiresAt)) {
                    log.info("[授权密钥] 用户{}已有更长的订阅有效期，不更新", userId);
                } else {
                    userBalanceMapper.updateSubscriptionEndDate(userId, expiresAt);
                }
            }

            log.info("[授权密钥] 用户{}激活订阅授权：{}天，到期时间：{}", userId, entity.getValue(), expiresAt);
        }

        // 更新授权密钥状态
        licenseKeyMapper.updateActivationStatus(entity.getId(), 0, now, expiresAt, deviceFingerprint);

        result.put("success", true);
        result.put("message", "授权激活成功");
        result.put("type", entity.getType());
        result.put("value", entity.getValue());
        result.put("expiresAt", expiresAt);

        return result;
    }

    /**
     * 验证授权密钥
     *
     * @param licenseKey 授权密钥
     * @param deviceFingerprint 设备指纹
     * @return 验证结果
     */
    public Map<String, Object> validateLicenseKey(String licenseKey, String deviceFingerprint) {
        Map<String, Object> result = new HashMap<>();

        LicenseKeyEntity entity = licenseKeyMapper.selectByLicenseKey(licenseKey);
        if (entity == null) {
            result.put("valid", false);
            result.put("message", "授权密钥不存在");
            return result;
        }

        if (entity.getStatus() == 2) {
            result.put("valid", false);
            result.put("message", "授权密钥已冻结");
            return result;
        }

        // 检查设备指纹是否匹配（已激活的情况）
        if (entity.getStatus() == 0 && entity.getDeviceFingerprint() != null) {
            if (!entity.getDeviceFingerprint().equals(deviceFingerprint)) {
                result.put("valid", false);
                result.put("message", "设备不匹配，请在绑定的设备上使用");
                return result;
            }

            // 检查是否过期
            if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now())) {
                result.put("valid", false);
                result.put("message", "授权已过期");
                return result;
            }
        }

        result.put("valid", true);
        result.put("type", entity.getType());
        result.put("value", entity.getValue());
        result.put("expiresAt", entity.getExpiresAt());

        return result;
    }

    /**
     * 冻结授权密钥
     *
     * @param licenseKeyId 授权密钥ID
     * @return 是否成功
     */
    @Transactional
    public boolean freezeLicenseKey(Long licenseKeyId) {
        int rows = licenseKeyMapper.updateStatus(licenseKeyId, 2);
        if (rows > 0) {
            log.info("[授权密钥] 冻结密钥ID：{}", licenseKeyId);
            return true;
        }
        return false;
    }

    /**
     * 生成随机授权码
     *
     * @param length 长度
     * @return 随机码
     */
    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_SET.charAt(RANDOM.nextInt(CHAR_SET.length())));
        }
        return sb.toString();
    }
}
