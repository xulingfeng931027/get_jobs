package com.getjobs.application.service;

import com.getjobs.application.utils.HardwareFingerprint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 客户端授权服务
 * 负责客户端机器指纹生成和本地许可证校验
 */
@Slf4j
@Service
public class ClientLicenseService {

    private static final String LICENSE_HEADER = "-----BEGIN LICENSE-----";
    private static final String LICENSE_FOOTER = "-----END LICENSE-----";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Value("${secure.license.file:license.key}")
    private String licenseFilePath;

    @Value("${secure.license.dev-mode:false}")
    private boolean devMode;

    private volatile LicenseInfo cachedLicense;

    /**
     * 验证客户端许可证是否有效
     */
    public boolean validateLicense() {
        // 开发模式跳过校验
        if (devMode) {
            log.info("开发模式，跳过客户端许可证校验");
            return true;
        }

        try {
            // 1. 加载许可证
            LicenseInfo license = loadLicense();
            if (license == null) {
                log.error("客户端许可证文件不存在或格式错误: {}", licenseFilePath);
                return false;
            }

            // 2. 机器指纹校验
            String machineId = HardwareFingerprint.getMachineId();
            if (!license.getMachineId().equals(machineId)) {
                log.error("客户端机器指纹不匹配: expected={}, actual={}", license.getMachineId(), machineId);
                return false;
            }

            // 3. 过期校验
            if (license.isExpired()) {
                log.error("客户端许可证已过期: {}", license.getExpireDate());
                return false;
            }

            cachedLicense = license;
            log.info("客户端许可证校验通过: user={}, expire={}", license.getUserName(), license.getExpireDate());
            return true;

        } catch (Exception e) {
            log.error("客户端许可证校验异常", e);
            return false;
        }
    }

    /**
     * 获取当前机器指纹
     */
    public String getMachineId() {
        return HardwareFingerprint.getMachineId();
    }

    /**
     * 加载本地许可证文件
     */
    private LicenseInfo loadLicense() {
        File licenseFile = new File(licenseFilePath);
        if (!licenseFile.exists()) {
            // 尝试从工作目录加载
            Path classpathLicense = Path.of(System.getProperty("user.dir"), "license.key");
            if (classpathLicense.toFile().exists()) {
                licenseFile = classpathLicense.toFile();
            } else {
                return null;
            }
        }

        try {
            String content = Files.readString(licenseFile.toPath(), StandardCharsets.UTF_8);
            return parseLicense(content);
        } catch (IOException e) {
            log.error("读取客户端许可证文件失败: {}", licenseFilePath, e);
            return null;
        }
    }

    /**
     * 解析许可证内容
     */
    LicenseInfo parseLicense(String content) {
        if (content == null || !content.contains(LICENSE_HEADER)) {
            return null;
        }

        try {
            int start = content.indexOf(LICENSE_HEADER) + LICENSE_HEADER.length();
            int end = content.indexOf(LICENSE_FOOTER);
            if (end <= start) {
                return null;
            }

            String body = content.substring(start, end).trim();
            String[] lines = body.split("\n");

            LicenseInfo license = new LicenseInfo();
            for (String line : lines) {
                int colonIndex = line.indexOf(':');
                if (colonIndex <= 0) continue;

                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();

                switch (key) {
                    case "MachineID" -> license.setMachineId(value);
                    case "ExpireDate" -> license.setExpireDate(LocalDate.parse(value, DATE_FORMATTER));
                    case "UserName" -> license.setUserName(value);
                    case "Tier" -> license.setTier(value);
                    case "Signature" -> license.setSignature(value);
                }
            }

            return license;

        } catch (Exception e) {
            log.error("解析客户端许可证失败", e);
            return null;
        }
    }

    /**
     * 验证许可证签名（简化版，生产环境应使用非对称加密）
     */
    private boolean verifySignature(LicenseInfo license) {
        if (license.getSignature() == null || license.getSignature().isEmpty()) {
            log.warn("客户端许可证无签名，仅做格式校验");
            return true;
        }

        try {
            String signContent = String.join("|",
                license.getMachineId(),
                license.getExpireDate().toString(),
                license.getUserName(),
                license.getTier() != null ? license.getTier() : ""
            );

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(signContent.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hash);

            return computed.equals(license.getSignature());

        } catch (Exception e) {
            log.error("客户端许可证签名验证异常", e);
            return false;
        }
    }

    /**
     * 许可证信息类
     */
    public static class LicenseInfo {
        private String machineId;
        private LocalDate expireDate;
        private String userName;
        private String tier;
        private String signature;

        public String getMachineId() { return machineId; }
        public void setMachineId(String machineId) { this.machineId = machineId; }

        public LocalDate getExpireDate() { return expireDate; }
        public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getTier() { return tier; }
        public void setTier(String tier) { this.tier = tier; }

        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }

        public boolean isExpired() {
            if (expireDate == null) return true;
            return expireDate.isBefore(LocalDate.now());
        }

        public boolean isValid() {
            return machineId != null && !machineId.isEmpty()
                && expireDate != null && !isExpired();
        }
    }
}
