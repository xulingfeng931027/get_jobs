package com.getjobs.worker.utils;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Page 资源池管理器（阶段 1：资源观测）
 * 
 * 功能：
 * - 为每个 Page 添加 lastUsedAt 时间戳追踪
 * - 提供 getPoolStats() 资源使用统计
 * - 不销毁 Page，仅观测
 * 
 * 阶段 2 扩展方向：
 * - 按需创建 Page（启动时不预创建）
 * - 空闲 Page 超过阈值后自动回收
 * - 动态 Page 池大小调整
 */
@Slf4j
public class PagePool {

    /**
     * Page 池存储
     */
    private final Map<String, PageEntry> pages = new ConcurrentHashMap<>();
    /**
     * 最大 Page 数
     */
    private final int maxPages;
    /**
     * 空闲回收超时（毫秒）
     */
    private final long idleTimeoutMs;

    /**
     * 构造函数
     *
     * @param maxPages 最大 Page 数
     * @param idleTimeoutMs 空闲回收超时（毫秒）
     */
    public PagePool(int maxPages, long idleTimeoutMs) {
        this.maxPages = maxPages;
        this.idleTimeoutMs = idleTimeoutMs;
        log.info("PagePool 初始化完成: maxPages={}, idleTimeout={}ms", maxPages, idleTimeoutMs);
    }

    /**
     * 注册 Page（初始化时调用）
     *
     * @param platform 平台名称
     * @param page Page 实例
     */
    public void register(String platform, Page page) {
        if (pages.size() >= maxPages) {
            log.warn("Page 池已满（{} 个），无法注册新 Page: {}", maxPages, platform);
        }

        PageEntry entry = new PageEntry(platform, page);
        pages.put(platform.toLowerCase(), entry);
        log.info("[{}] 注册 Page 到资源池", platform);
    }

    /**
     * 获取 Page（自动标记使用）
     *
     * @param platform 平台名称
     * @return Page 实例
     */
    public Page acquire(String platform) {
        String key = platform.toLowerCase();
        PageEntry entry = pages.get(key);

        if (entry == null) {
            log.warn("[{}] Page 未注册到资源池", platform);
            return null;
        }

        entry.markUsed();
        return entry.getPage();
    }

    /**
     * 释放 Page（仅标记，不销毁）
     *
     * @param platform 平台名称
     */
    public void release(String platform) {
        String key = platform.toLowerCase();
        PageEntry entry = pages.get(key);

        if (entry != null) {
            log.debug("[{}] 释放 Page（标记空闲）", platform);
        }
    }

    /**
     * 获取 Page 条目
     *
     * @param platform 平台名称
     * @return PageEntry 实例
     */
    public PageEntry getPageEntry(String platform) {
        return pages.get(platform.toLowerCase());
    }

    /**
     * 获取资源池统计信息
     *
     * @return 统计信息字符串
     */
    public String getPoolStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PagePool 资源池统计 ===\n");
        sb.append("总 Page 数: ").append(pages.size()).append(" / ").append(maxPages).append("\n\n");

        for (Map.Entry<String, PageEntry> entry : pages.entrySet()) {
            PageEntry pe = entry.getValue();
            long idleMinutes = pe.getIdleTimeMs() / (1000 * 60);
            long lifetimeMinutes = pe.getLifetimeMs() / (1000 * 60);

            sb.append(String.format("[%s]\n", pe.getPlatform().toUpperCase()));
            sb.append("  访问次数: ").append(pe.getAccessCount()).append("\n");
            sb.append("  空闲时间: ").append(idleMinutes).append(" 分钟\n");
            sb.append("  存在时间: ").append(lifetimeMinutes).append(" 分钟\n");
            sb.append("  状态: ").append(pe.getPage() != null ? "活跃" : "已关闭").append("\n\n");
        }

        return sb.toString();
    }

    /**
     * 获取空闲的 Page 列表（超过阈值）
     *
     * @return 空闲 Page 列表
     */
    public java.util.List<String> getIdlePlatforms() {
        java.util.List<String> idlePlatforms = new java.util.ArrayList<>();

        for (Map.Entry<String, PageEntry> entry : pages.entrySet()) {
            if (entry.getValue().getIdleTimeMs() > idleTimeoutMs) {
                idlePlatforms.add(entry.getKey());
            }
        }

        return idlePlatforms;
    }

    /**
     * 清理空闲 Page（阶段 2 实施）
     *
     * @return 清理的 Page 数量
     */
    public int cleanupIdlePages() {
        // 阶段 1：仅统计，不清理
        java.util.List<String> idlePlatforms = getIdlePlatforms();

        if (!idlePlatforms.isEmpty()) {
            log.info("发现 {} 个空闲平台: {}", idlePlatforms.size(), idlePlatforms);
            log.info("阶段 1 不执行清理操作");
        }

        return 0;
    }

    /**
     * 获取活跃 Page 数
     *
     * @return 活跃 Page 数
     */
    public int getActivePageCount() {
        int count = 0;
        for (PageEntry entry : pages.values()) {
            if (entry.getPage() != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取资源池大小
     *
     * @return 资源池大小
     */
    public int getSize() {
        return pages.size();
    }

    /**
     * 检查平台是否已注册
     *
     * @param platform 平台名称
     * @return 是否已注册
     */
    public boolean isRegistered(String platform) {
        return pages.containsKey(platform.toLowerCase());
    }

    /**
     * 清空资源池（仅在测试或重置时使用）
     */
    public void clear() {
        pages.clear();
        log.info("已清空 PagePool");
    }

    /**
     * 获取最大 Page 数
     *
     * @return 最大 Page 数
     */
    public int getMaxPages() {
        return maxPages;
    }

    /**
     * 获取空闲超时时间
     *
     * @return 空闲超时时间（毫秒）
     */
    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    /**
     * Page 条目信息
     */
    public static class PageEntry {
        private final String platform;
        private final Page page;
        private Instant lastUsedAt;
        private int accessCount;
        private Instant createdAt;

        public PageEntry(String platform, Page page) {
            this.platform = platform;
            this.page = page;
            this.createdAt = Instant.now();
            this.lastUsedAt = Instant.now();
            this.accessCount = 0;
        }

        /**
         * 标记为已使用
         */
        public void markUsed() {
            this.lastUsedAt = Instant.now();
            this.accessCount++;
        }

        /**
         * 获取空闲时间（毫秒）
         */
        public long getIdleTimeMs() {
            return Duration.between(lastUsedAt, Instant.now()).toMillis();
        }

        /**
         * 获取 Page 存在时间（毫秒）
         */
        public long getLifetimeMs() {
            return Duration.between(createdAt, Instant.now()).toMillis();
        }

        // Getters
        public String getPlatform() { return platform; }
        public Page getPage() { return page; }
        public Instant getLastUsedAt() { return lastUsedAt; }
        public int getAccessCount() { return accessCount; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
