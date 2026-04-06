package com.getjobs.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Playwright 浏览器安装检查
 * 启动时检查浏览器是否安装，未安装则自动安装
 */
@Slf4j
@Component
@Order(1) // 在 PlaywrightManager 之前执行
public class PlaywrightBrowserInstaller implements ApplicationRunner {

    @Value("${playwright.auto-install:true}")
    private boolean autoInstall;

    @Value("${playwright.browser-channel:chromium}")
    private String browserChannel;

    @Value("${playwright.install-dir:}")
    private String installDir;

    @Override
    public void run(ApplicationArguments args) {
        if (!autoInstall) {
            log.info("Playwright 浏览器自动安装已禁用");
            return;
        }

        log.info("========================================");
        log.info("  Playwright 浏览器安装检查");
        log.info("========================================");

        try {
            // 检查浏览器是否已安装
            if (isBrowserInstalled()) {
                log.info("✓ 浏览器已安装");
            } else {
                log.warn("✗ 浏览器未安装或安装不完整");
                log.info("开始自动安装浏览器...");

                installBrowser();

                // 再次检查
                if (isBrowserInstalled()) {
                    log.info("✓ 浏览器安装成功");
                } else {
                    log.error("✗ 浏览器安装失败");
                    log.error("请手动执行: npx playwright install {}", browserChannel);
                }
            }

            // 显示浏览器路径
            showBrowserPath();

        } catch (Exception e) {
            log.error("Playwright 浏览器检查失败: {}", e.getMessage());
            log.warn("请手动执行: npx playwright install {}", browserChannel);
        }

        log.info("========================================");
    }

    /**
     * 检查浏览器是否已安装
     */
    private boolean isBrowserInstalled() {
        Path browserPath = getBrowserPath();
        if (browserPath == null) {
            return false;
        }

        Path executable = browserPath.resolve(getExecutableName());
        boolean exists = Files.exists(executable);

        if (!exists) {
            log.debug("浏览器可执行文件不存在: {}", executable);
        }

        return exists;
    }

    /**
     * 获取浏览器安装路径
     */
    private Path getBrowserPath() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        String basePath;
        if (os.contains("windows")) {
            basePath = userHome + "\\AppData\\Local\\ms-playwright";
        } else if (os.contains("mac")) {
            basePath = userHome + "/Library/Caches/ms-playwright";
        } else {
            basePath = userHome + "/.cache/ms-playwright";
        }

        // 自定义安装目录优先
        if (installDir != null && !installDir.isEmpty()) {
            basePath = installDir;
        }

        // 根据浏览器类型和版本确定路径
        String browserPath;
        switch (browserChannel.toLowerCase()) {
            case "chromium" -> browserPath = basePath + "/chromium-1161/chrome-win";
            case "firefox" -> browserPath = basePath + "/firefox-1522";
            case "webkit" -> browserPath = basePath + "/webkit-2032";
            default -> browserPath = basePath + "/chromium-1161/chrome-win";
        }

        return Paths.get(browserPath);
    }

    /**
     * 获取可执行文件名
     */
    private String getExecutableName() {
        String os = System.getProperty("os.name").toLowerCase();
        switch (browserChannel.toLowerCase()) {
            case "chromium" -> {
                return os.contains("windows") ? "chrome.exe" : "chrome";
            }
            case "firefox" -> {
                return os.contains("windows") ? "firefox.exe" : "firefox";
            }
            case "webkit" -> {
                return os.contains("windows") ? "Safari.exe" : "Safari";
            }
            default -> {
                return "chrome";
            }
        }
    }

    /**
     * 安装浏览器
     */
    private void installBrowser() {
        try {
            log.info("开始安装 {} 浏览器...", browserChannel);

            // 方式1: 使用 Playwright API 安装
            com.microsoft.playwright.Playwright.create().close(); // 先创建实例

            // 触发浏览器下载
            ProcessBuilder pb;
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows")) {
                pb = new ProcessBuilder("cmd.exe", "/c", "npx playwright install " + browserChannel);
            } else {
                pb = new ProcessBuilder("/bin/sh", "-c", "npx playwright install " + browserChannel);
            }

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("✓ 浏览器安装完成");
            } else {
                log.error("✗ 浏览器安装失败，退出码: {}", exitCode);
            }

        } catch (IOException e) {
            log.error("IO 错误: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("安装被中断");
        }
    }

    /**
     * 显示浏览器路径
     */
    private void showBrowserPath() {
        Path browserPath = getBrowserPath();
        if (browserPath != null) {
            log.info("浏览器路径: {}", browserPath);
        }
    }
}
