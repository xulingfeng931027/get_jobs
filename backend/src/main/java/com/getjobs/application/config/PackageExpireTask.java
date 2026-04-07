package com.getjobs.application.config;

import com.getjobs.application.service.PackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 套餐定时任务配置
 */
@Slf4j
@Configuration
public class PackageExpireTask {

    @Autowired
    private PackageService packageService;

    /**
     * 每天凌晨1点处理过期套餐
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processExpiredPackages() {
        log.info("[定时任务] 开始处理过期套餐");
        try {
            packageService.processExpiredPackages();
        } catch (Exception e) {
            log.error("[定时任务] 处理过期套餐失败: {}", e.getMessage(), e);
        }
    }
}
