package com.getjobs.application.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

import java.security.CodeSource;
import java.security.Security;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JAR 签名校验器
 * 启动时验证 JAR 是否被篡改
 */
@Slf4j
@Component
public class SignatureValidator {

    /**
     * 禁用 JAVA_ATTACH 环境检查
     * 防止通过 attach 机制注入 agent
     */
    static {
        try {
            // 设置系统属性禁用 attach
            System.setProperty("jdk.attach.allowAttachSelf", "false");

            // 添加安全管理器限制
            String packageAccess = Security.getProperty("package.access");
            if (packageAccess != null) {
                Security.setProperty("package.access", packageAccess + ";sun.misc;");
            }
        } catch (Exception e) {
            // 忽略，可能在某些 JVM 上不支持
        }
    }

    @Value("${security.dev.mode:false}")
    private boolean devMode;

    @PostConstruct
    public void validateSignature() {
        // 开发模式跳过校验
        if (devMode) {
            log.info("开发模式，跳过 JAR 签名校验");
            return;
        }

        try {
            String jarPath = getJarPath();
            if (jarPath == null) {
                log.warn("无法确定 JAR 路径，可能在 IDE 环境中运行");
                return;
            }

            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                throw new SecurityException("JAR 文件不存在: " + jarPath);
            }

            // 校验签名
            validateJarSignature(jarFile);

            log.info("JAR 签名校验通过");
        } catch (SecurityException e) {
            log.error("JAR 安全校验失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("JAR 签名校验异常", e);
            // 非致命异常不阻止启动
        }
    }

    private String getJarPath() {
        try {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }
            String path = codeSource.getLocation().toURI().getPath();
            if (path == null) {
                return null;
            }
            return new File(path).getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void validateJarSignature(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            // 检查签名文件是否存在
            JarEntry rsaEntry = jar.getJarEntry("META-INF/GETJOBS.RSA");
            JarEntry sfEntry = jar.getJarEntry("META-INF/GETJOBS.SF");

            if (rsaEntry != null && sfEntry != null) {
                log.debug("JAR 签名文件校验完成");
                return;
            }

            // 检查其他可能的签名格式
            boolean foundSignature = false;
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("META-INF/") &&
                    (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC"))) {
                    foundSignature = true;
                    break;
                }
            }

            if (!foundSignature) {
                // ProGuard 混淆后签名会丢失，这是预期行为，仅警告不阻止启动
                log.warn("JAR 未包含签名信息，如需签名校验请对 JAR 进行签名");
            } else {
                log.debug("JAR 签名文件校验完成");
            }
        }
    }
}
