package com.getjobs.application.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * 反调试检测
 * 启动时检测调试器、IDE 和虚拟机环境
 */
@Slf4j
@Component
public class AntiDebugCheck implements ApplicationRunner {

    @Value("${security.dev.mode:false}")
    private boolean devMode;

    @Override
    public void run(ApplicationArguments args) {
        if (devMode) {
            log.info("开发模式，跳过反调试检测");
            return;
        }

        log.info("开始安全环境检测...");

        checkDebugger();
        checkIDE();
        checkVirtualMachine();
        checkSuspiciousProperties();

        log.info("安全环境检测完成");
    }

    /**
     * 检测调试器附加
     */
    private void checkDebugger() {
        try {
            // 检查 JVM 输入参数中是否包含 jdwp
            List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            String allArgs = inputArgs.toString();

            if (allArgs.contains("jdwp") || allArgs.contains("agentlib:jdwp")) {
                log.error("检测到调试器附加: {}", allArgs);
                exitWithError("检测到调试器，程序拒绝运行");
            }

            // 检查是否启用了调试端口
            if (allArgs.contains("dt_socket") || allArgs.contains("dt_shmem")) {
                log.error("检测到调试端口: {}", allArgs);
                exitWithError("检测到调试端口，程序拒绝运行");
            }

            // 检查是否在 suspend 模式（调试挂起）
            if (allArgs.contains("suspend=y") || allArgs.contains("suspend=Y")) {
                log.error("检测到调试挂起模式: {}", allArgs);
                exitWithError("检测到调试挂起模式，程序拒绝运行");
            }

            log.debug("调试器检测通过");

        } catch (Exception e) {
            log.warn("调试器检测异常: {}", e.getMessage());
        }
    }

    /**
     * 检测 IDE 环境
     */
    private void checkIDE() {
        try {
            String classPath = System.getProperty("java.class.path", "");
            String javaHome = System.getProperty("java.home", "");
            String userDir = System.getProperty("user.dir", "");

            // 检测 IDEA
            if (classPath.contains("idea_rt.jar") ||
                classPath.contains("idea.jar") ||
                userDir.contains("IntelliJ IDEA") ||
                userDir.contains(".idea")) {
                log.error("检测到 IntelliJ IDEA 环境");
                exitWithError("检测到 IDE 环境，程序拒绝运行");
            }

            // 检测 Eclipse
            if (classPath.contains("eclipse.ini") ||
                classPath.contains("eclipse.jar") ||
                userDir.contains("eclipse")) {
                log.error("检测到 Eclipse 环境");
                exitWithError("检测到 IDE 环境，程序拒绝运行");
            }

            // 检测 VS Code
            if (classPath.contains("code.jar") ||
                System.getProperty("vscode.jre.path") != null) {
                log.error("检测到 VS Code 环境");
                exitWithError("检测到 IDE 环境，程序拒绝运行");
            }

            // 检测 NetBeans
            if (classPath.contains("netbeans") || userDir.contains("netbeans")) {
                log.error("检测到 NetBeans 环境");
                exitWithError("检测到 IDE 环境，程序拒绝运行");
            }

            log.debug("IDE 检测通过");

        } catch (Exception e) {
            log.warn("IDE 检测异常: {}", e.getMessage());
        }
    }

    /**
     * 检测虚拟机/Docker 环境
     */
    private void checkVirtualMachine() {
        try {
            String osName = System.getProperty("os.name", "").toLowerCase();

            // Linux 下检测 Docker
            if (osName.contains("linux")) {
                // 检查 cgroup
                String cgroup = readFile("/proc/1/cgroup");
                if (cgroup != null && (cgroup.contains("docker") || cgroup.contains("containerd"))) {
                    log.error("检测到 Docker/Linux 容器环境");
                    exitWithError("检测到容器环境，程序拒绝运行");
                }

                // 检查 .dockerenv 文件
                if (new java.io.File("/.dockerenv").exists()) {
                    log.error("检测到 .dockerenv 文件");
                    exitWithError("检测到容器环境，程序拒绝运行");
                }
            }

            // 检测 VMware/VirtualBox
            String systemInfo = getSystemProductName();
            if (systemInfo != null && (
                systemInfo.toLowerCase().contains("vmware") ||
                systemInfo.toLowerCase().contains("virtualbox") ||
                systemInfo.toLowerCase().contains("qemu") ||
                systemInfo.toLowerCase().contains("kvm")
            )) {
                log.error("检测到虚拟机环境: {}", systemInfo);
                // 虚拟机检测可选，不阻止运行但记录日志
                log.warn("检测到虚拟机环境，程序可能处于受限环境中");
            }

            log.debug("虚拟机检测完成");

        } catch (Exception e) {
            log.warn("虚拟机检测异常: {}", e.getMessage());
        }
    }

    /**
     * 检测可疑的系统属性
     */
    private void checkSuspiciousProperties() {
        try {
            // 检测是否启用了某些调试相关的系统属性
            String[] suspiciousProps = {
                "java.security.debug",
                "sun.zip.disableMemoryMapping",
                "jdk.attach.allowAttachSelf"
            };

            for (String prop : suspiciousProps) {
                String value = System.getProperty(prop);
                if (value != null && !value.isEmpty()) {
                    log.warn("检测到可疑系统属性: {}={}", prop, value);
                }
            }

            // 检测安全管理器是否被禁用
            String securityManager = System.getProperty("java.security.manager");
            if (securityManager != null && securityManager.equals("disable")) {
                log.error("检测到安全管理器被禁用");
                exitWithError("检测到安全限制被绕过，程序拒绝运行");
            }

            log.debug("可疑属性检测完成");

        } catch (Exception e) {
            log.warn("可疑属性检测异常: {}", e.getMessage());
        }
    }

    private String readFile(String path) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)));
        } catch (Exception e) {
            return null;
        }
    }

    private String getSystemProductName() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
                Process process = Runtime.getRuntime().exec("wmic computersystem get model");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                reader.readLine(); // 跳过标题
                reader.readLine(); // 跳过空行
                return reader.readLine();
            } else if (new java.io.File("/sys/class/dmi/id/product_name").exists()) {
                return readFile("/sys/class/dmi/id/product_name").trim();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private void exitWithError(String message) {
        log.error(message);
        // 在非生产环境可能需要退出
        // System.exit(1);
        // 但考虑到可能误判，暂时只记录错误而不退出
        // 生产部署时可启用
        if (!devMode) {
            throw new SecurityException(message);
        }
    }
}
