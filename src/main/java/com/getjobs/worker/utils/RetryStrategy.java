package com.getjobs.worker.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * 指数退避重试策略工具类
 * 
 * 使用场景：
 * - 导航重试（替代固定 2s 延迟）
 * - Cookie 加载重试
 * - 元素点击重试
 * 
 * 重试序列示例（baseDelay=1000ms, maxDelay=10000ms）：
 * - attempt 0: 1000-1500ms
 * - attempt 1: 2000-2500ms
 * - attempt 2: 4000-4500ms
 */
@Slf4j
public class RetryStrategy {

    private static final Random RANDOM = new Random();

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /**
     * 默认基础延迟（毫秒）
     */
    private static final long DEFAULT_BASE_DELAY_MS = 1000;

    /**
     * 默认最大延迟（毫秒）
     */
    private static final long DEFAULT_MAX_DELAY_MS = 10000;

    /**
     * 使用默认策略执行（3次重试，1s基础延迟，10s最大延迟）
     *
     * @param task 要执行的任务
     * @param taskName 任务名称（用于日志）
     * @return 任务执行结果
     * @throws Exception 重试耗尽后的异常
     */
    public static <T> T executeWithRetry(Callable<T> task, String taskName) throws Exception {
        return executeWithRetry(task, taskName, DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY_MS, DEFAULT_MAX_DELAY_MS);
    }

    /**
     * 使用自定义参数执行重试
     *
     * @param task 要执行的任务
     * @param taskName 任务名称（用于日志）
     * @param maxRetries 最大重试次数
     * @param baseDelayMs 基础延迟（毫秒）
     * @param maxDelayMs 最大延迟（毫秒）
     * @return 任务执行结果
     * @throws Exception 重试耗尽后的异常
     */
    public static <T> T executeWithRetry(Callable<T> task, String taskName, 
                                          int maxRetries, long baseDelayMs, long maxDelayMs) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("[{}] 重试第 {} 次", taskName, attempt);
                }
                
                T result = task.call();
                
                if (attempt > 0) {
                    log.info("[{}] 重试成功（第 {} 次）", taskName, attempt);
                }
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("[{}] 第 {} 次尝试失败: {}", taskName, attempt + 1, e.getMessage());

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt, baseDelayMs, maxDelayMs);
                    log.info("[{}] 等待 {}ms 后重试...", taskName, delay);
                    Thread.sleep(delay);
                }
            }
        }

        throw new RuntimeException(String.format("[%s] 重试 %d 次后仍然失败", taskName, maxRetries + 1), lastException);
    }

    /**
     * 执行无返回值的任务
     *
     * @param task 要执行的任务
     * @param taskName 任务名称
     * @throws Exception 重试耗尽后的异常
     */
    public static void executeWithRetry(Runnable task, String taskName) throws Exception {
        executeWithRetry(() -> {
            task.run();
            return null;
        }, taskName);
    }

    /**
     * 计算指数退避延迟（含随机抖动）
     *
     * 公式：delay = min(baseDelay * 2^attempt + random(0, 500), maxDelay)
     *
     * @param attempt 当前尝试次数（从 0 开始）
     * @param baseDelayMs 基础延迟
     * @param maxDelayMs 最大延迟
     * @return 实际延迟时间（毫秒）
     */
    private static long calculateDelay(int attempt, long baseDelayMs, long maxDelayMs) {
        long exponentialDelay = baseDelayMs * (long) Math.pow(2, attempt);
        long jitter = RANDOM.nextInt(500); // 0-500ms 随机抖动
        long delay = Math.min(exponentialDelay + jitter, maxDelayMs);
        return delay;
    }

    /**
     * 简单重试（固定延迟，不含指数退避）
     * 适用于不需要复杂重试逻辑的场景
     *
     * @param task 要执行的任务
     * @param taskName 任务名称
     * @param maxRetries 最大重试次数
     * @param fixedDelayMs 固定延迟（毫秒）
     * @return 任务执行结果
     * @throws Exception 重试耗尽后的异常
     */
    public static <T> T executeWithFixedDelay(Callable<T> task, String taskName, 
                                               int maxRetries, long fixedDelayMs) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    log.info("[{}] 固定延迟重试第 {} 次", taskName, attempt);
                }
                
                T result = task.call();
                
                if (attempt > 0) {
                    log.info("[{}] 固定延迟重试成功（第 {} 次）", taskName, attempt);
                }
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("[{}] 第 {} 次尝试失败: {}", taskName, attempt + 1, e.getMessage());

                if (attempt < maxRetries) {
                    log.info("[{}] 等待 {}ms 后重试...", taskName, fixedDelayMs);
                    Thread.sleep(fixedDelayMs);
                }
            }
        }

        throw new RuntimeException(String.format("[%s] 固定延迟重试 %d 次后仍然失败", taskName, maxRetries + 1), lastException);
    }
}
