package com.getjobs.worker.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Page 级别并发操作锁管理器
 * 
 * 解决问题：
 * - 导航并发冲突（"Object doesn't exist" 错误）
 * - 同一 Page 的并发操作冲突
 * 
 * 使用场景：
 * - PlaywrightManager 初始化平台时
 * - 投递任务执行时
 * - 登录状态检测时
 */
@Slf4j
public class PageLockRegistry {

    /**
     * 平台锁映射
     */
    private static final Map<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    /**
     * 默认锁超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    /**
     * 获取指定平台的锁（不存在则创建）
     *
     * @param platform 平台名称（boss/liepin/job51/zhilian）
     * @return ReentrantLock 实例
     */
    public static ReentrantLock getLock(String platform) {
        return LOCKS.computeIfAbsent(platform.toLowerCase(), k -> new ReentrantLock());
    }

    /**
     * 尝试获取锁（使用默认超时 10 秒）
     *
     * @param platform 平台名称
     * @return 是否成功获取锁
     */
    public static boolean tryLock(String platform) {
        return tryLock(platform, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 尝试获取锁（自定义超时时间）
     *
     * @param platform 平台名称
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否成功获取锁
     */
    public static boolean tryLock(String platform, long timeoutSeconds) {
        ReentrantLock lock = getLock(platform);
        try {
            boolean acquired = lock.tryLock(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
            if (acquired) {
                log.debug("[{}] 成功获取 Page 锁", platform);
            } else {
                log.warn("[{}] 获取 Page 锁超时（{}s），可能存在并发操作", platform, timeoutSeconds);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[{}] 获取 Page 锁被中断", platform, e);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param platform 平台名称
     */
    public static void unlock(String platform) {
        ReentrantLock lock = getLock(platform);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[{}] 释放 Page 锁", platform);
        } else {
            log.warn("[{}] 尝试释放未持有的 Page 锁", platform);
        }
    }

    /**
     * 带锁执行任务（使用默认超时）
     *
     * @param platform 平台名称
     * @param task 要执行的任务
     * @return 是否执行成功
     */
    public static boolean executeWithLock(String platform, Runnable task) {
        return executeWithLock(platform, task, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 带锁执行任务（自定义超时）
     *
     * @param platform 平台名称
     * @param task 要执行的任务
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    public static boolean executeWithLock(String platform, Runnable task, long timeoutSeconds) {
        if (tryLock(platform, timeoutSeconds)) {
            try {
                task.run();
                return true;
            } finally {
                unlock(platform);
            }
        }
        return false;
    }

    /**
     * 带锁执行任务（带返回值）
     *
     * @param platform 平台名称
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return 任务返回值，获取锁失败返回 null
     */
    public static <T> T executeWithLock(String platform, java.util.concurrent.Callable<T> task) {
        return executeWithLock(platform, task, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 带锁执行任务（带返回值，自定义超时）
     *
     * @param platform 平台名称
     * @param task 要执行的任务
     * @param timeoutSeconds 超时时间（秒）
     * @param <T> 返回值类型
     * @return 任务返回值，获取锁失败返回 null
     */
    public static <T> T executeWithLock(String platform, java.util.concurrent.Callable<T> task, long timeoutSeconds) {
        if (tryLock(platform, timeoutSeconds)) {
            try {
                return task.call();
            } catch (Exception e) {
                log.error("[{}] 带锁执行任务异常", platform, e);
                throw new RuntimeException(e);
            } finally {
                unlock(platform);
            }
        }
        log.warn("[{}] 获取锁失败，任务未执行", platform);
        return null;
    }

    /**
     * 检查锁是否被持有
     *
     * @param platform 平台名称
     * @return 是否被持有
     */
    public static boolean isLocked(String platform) {
        ReentrantLock lock = getLock(platform);
        return lock.isLocked();
    }

    /**
     * 获取所有锁的状态信息
     *
     * @return 状态描述字符串
     */
    public static String getLockStatus() {
        StringBuilder sb = new StringBuilder("PageLockRegistry 状态:\n");
        for (Map.Entry<String, ReentrantLock> entry : LOCKS.entrySet()) {
            ReentrantLock lock = entry.getValue();
            sb.append(String.format("  [%s] locked=%s, held=%s, queued=%d\n",
                    entry.getKey(),
                    lock.isLocked(),
                    lock.isHeldByCurrentThread(),
                    lock.getQueueLength()));
        }
        return sb.toString();
    }

    /**
     * 清理所有锁（仅在测试或重置时使用）
     */
    public static void clearAll() {
        for (Map.Entry<String, ReentrantLock> entry : LOCKS.entrySet()) {
            ReentrantLock lock = entry.getValue();
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        LOCKS.clear();
        log.info("已清理所有 Page 锁");
    }
}
