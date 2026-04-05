package com.getjobs.worker.utils;

import com.getjobs.application.entity.CookieEntity;
import com.getjobs.application.service.CookieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cookie 生命周期管理器
 * 
 * 功能：
 * - 记录 Cookie 保存时间戳
 * - 检测即将过期的 Cookie
 * - 提供自动刷新触发机制
 * 
 * 各平台 Cookie 预期有效期：
 * - Boss 直聘：约 7 天
 * - 猎聘：约 30 天
 * - 51job：约 15 天
 * - 智联招聘：约 14 天
 */
@Slf4j
@Component
public class CookieManager {

    /**
     * 平台 Cookie 预期有效期（毫秒）
     */
    private static final Map<String, Duration> EXPECTED_LIFETIMES = new ConcurrentHashMap<>();

    static {
        EXPECTED_LIFETIMES.put("boss", Duration.ofDays(7));
        EXPECTED_LIFETIMES.put("liepin", Duration.ofDays(30));
        EXPECTED_LIFETIMES.put("job51", Duration.ofDays(15));
        EXPECTED_LIFETIMES.put("zhilian", Duration.ofDays(14));
    }

    /**
     * Cookie 保存记录
     */
    private final Map<String, CookieRecord> cookieRecords = new ConcurrentHashMap<>();
    @Autowired
    private CookieService cookieService;
    /**
     * Cookie 过期前刷新时间（默认 1 小时）
     */
    @Value("${playwright.cookie-refresh-before-expiry:3600000}")
    private long refreshBeforeExpiryMs;

    /**
     * 记录 Cookie 保存事件
     *
     * @param platform 平台名称
     */
    public void recordCookieSave(String platform) {
        Duration expectedLifetime = EXPECTED_LIFETIMES.getOrDefault(platform.toLowerCase(), Duration.ofDays(7));
        cookieRecords.put(platform.toLowerCase(), new CookieRecord(Instant.now(), expectedLifetime));
        log.info("[{}] 记录 Cookie 保存，预期有效期: {}", platform, expectedLifetime.toDays() + " 天");
    }

    /**
     * 检查 Cookie 是否即将过期
     *
     * @param platform 平台名称
     * @return 是否即将过期
     */
    public boolean isExpiringSoon(String platform) {
        String key = platform.toLowerCase();
        CookieRecord record = cookieRecords.get(key);
        if (record == null) {
            log.warn("[{}] 未找到 Cookie 保存记录，无法检测过期状态", platform);
            return true; // 保守返回，认为需要刷新
        }
        return record.isExpiringSoon(refreshBeforeExpiryMs);
    }

    /**
     * 检查 Cookie 是否已过期
     *
     * @param platform 平台名称
     * @return 是否已过期
     */
    public boolean isExpired(String platform) {
        String key = platform.toLowerCase();
        CookieRecord record = cookieRecords.get(key);
        if (record == null) {
            log.warn("[{}] 未找到 Cookie 保存记录，视为已过期", platform);
            return true;
        }
        return record.isExpired();
    }

    /**
     * 获取 Cookie 剩余有效时间
     *
     * @param platform 平台名称
     * @return 剩余时间（毫秒），-1 表示无记录
     */
    public long getRemainingTimeMs(String platform) {
        String key = platform.toLowerCase();
        CookieRecord record = cookieRecords.get(key);
        if (record == null) {
            return -1;
        }
        return record.getRemainingTimeMs();
    }

    /**
     * 检查是否需要刷新 Cookie
     * （即将过期或已过期）
     *
     * @param platform 平台名称
     * @return 是否需要刷新
     */
    public boolean needsRefresh(String platform) {
        String key = platform.toLowerCase();
        CookieRecord record = cookieRecords.get(key);
        if (record == null) {
            return true;
        }
        return record.isExpired() || record.isExpiringSoon(refreshBeforeExpiryMs);
    }

    /**
     * 标记 Cookie 需要刷新（由登录检测触发）
     *
     * @param platform 平台名称
     */
    public void markNeedsRefresh(String platform) {
        String key = platform.toLowerCase();
        CookieRecord record = cookieRecords.get(key);
        if (record != null) {
            record.needsRefresh = true;
            log.warn("[{}] 检测到 Cookie 失效，已标记需要刷新", platform);
        }
    }

    /**
     * 刷新 Cookie 后调用（更新保存时间）
     *
     * @param platform 平台名称
     */
    public void markRefreshed(String platform) {
        recordCookieSave(platform);
        log.info("[{}] Cookie 已刷新，重置保存时间", platform);
    }

    /**
     * 获取所有需要刷新的平台列表
     *
     * @return 需要刷新的平台列表
     */
    public java.util.List<String> getPlatformsNeedingRefresh() {
        java.util.List<String> platforms = new java.util.ArrayList<>();
        for (String platform : EXPECTED_LIFETIMES.keySet()) {
            if (needsRefresh(platform)) {
                platforms.add(platform);
            }
        }
        return platforms;
    }

    /**
     * 获取 Cookie 状态摘要（用于调试/监控）
     *
     * @return 状态摘要字符串
     */
    public String getStatusSummary() {
        StringBuilder sb = new StringBuilder("Cookie 状态:\n");
        for (String platform : EXPECTED_LIFETIMES.keySet()) {
            String key = platform.toLowerCase();
            CookieRecord record = cookieRecords.get(key);
            if (record == null) {
                sb.append(String.format("  [%s] 无记录\n", platform));
            } else {
                long remainingMs = record.getRemainingTimeMs();
                String status;
                if (record.isExpired()) {
                    status = "已过期";
                } else if (record.isExpiringSoon(refreshBeforeExpiryMs)) {
                    status = "即将过期";
                } else {
                    status = "有效";
                }
                sb.append(String.format("  [%s] %s, 剩余: %d 小时\n",
                        platform, status, remainingMs / (1000 * 60 * 60)));
            }
        }
        return sb.toString();
    }

    /**
     * 初始化 Cookie 记录（从数据库加载后调用）
     *
     * @param platform 平台名称
     */
    public void initFromDatabase(String platform) {
        CookieEntity cookieEntity = cookieService.getCookieByPlatform(platform);
        if (cookieEntity != null && cookieEntity.getUpdatedAt() != null) {
            Instant lastSaved = cookieEntity.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC);
            Duration expectedLifetime = EXPECTED_LIFETIMES.getOrDefault(platform.toLowerCase(), Duration.ofDays(7));
            cookieRecords.put(platform.toLowerCase(), new CookieRecord(lastSaved, expectedLifetime));
            log.info("[{}] 从数据库初始化 Cookie 记录，最后更新: {}", platform, lastSaved);
        } else {
            log.warn("[{}] 数据库中无 Cookie 记录", platform);
        }
    }

    /**
     * Cookie 保存记录
     */
    private static class CookieRecord {
        Instant lastSaved;
        Duration expectedLifetime;
        boolean needsRefresh;

        CookieRecord(Instant lastSaved, Duration expectedLifetime) {
            this.lastSaved = lastSaved;
            this.expectedLifetime = expectedLifetime;
            this.needsRefresh = false;
        }

        /**
         * 检查是否即将过期
         */
        boolean isExpiringSoon(long refreshBeforeMs) {
            Instant now = Instant.now();
            Instant predictedExpiry = lastSaved.plus(expectedLifetime);
            Instant refreshThreshold = predictedExpiry.minusMillis(refreshBeforeMs);
            return now.isAfter(refreshThreshold);
        }

        /**
         * 检查是否已过期
         */
        boolean isExpired() {
            Instant now = Instant.now();
            Instant predictedExpiry = lastSaved.plus(expectedLifetime);
            return now.isAfter(predictedExpiry);
        }

        /**
         * 获取剩余有效时间（毫秒）
         */
        long getRemainingTimeMs() {
            Instant now = Instant.now();
            Instant predictedExpiry = lastSaved.plus(expectedLifetime);
            return Duration.between(now, predictedExpiry).toMillis();
        }
    }
}
