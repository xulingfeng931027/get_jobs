package com.getjobs.worker.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 机器唯一标识提供者
 * 使用 MAC 地址 + 机器序列号生成唯一的机器指纹
 * 用于标识 Playwright 浏览器 Cookie 的存储 key，避免多机器之间 cookie 串号
 */
@Slf4j
public class MachineIdProvider {

    private static final AtomicReference<String> CACHED_MACHINE_ID = new AtomicReference<>();

    /**
     * 获取机器唯一标识
     * 优先使用缓存，避免重复执行系统命令
     *
     * @return 机器唯一标识（MD5 哈希后的十六进制字符串）
     */
    public static String getMachineId() {
        String cached = CACHED_MACHINE_ID.get();
        if (cached != null && !cached.isBlank()) {
            return cached;
        }

        String machineId = generateMachineId();
        CACHED_MACHINE_ID.set(machineId);
        log.info("[MachineId] 机器标识: {}", machineId);
        return machineId;
    }

    /**
     * 生成机器唯一标识
     * 组合 MAC 地址和机器序列号，MD5 哈希后取前 16 位
     */
    private static String generateMachineId() {
        try {
            List<String> identifiers = new ArrayList<>();

            // 1. 获取 MAC 地址
            String macAddress = getMacAddress();
            if (macAddress != null && !macAddress.isBlank()) {
                identifiers.add(macAddress);
                log.debug("[MachineId] MAC地址: {}", macAddress);
            }

            // 2. 获取机器序列号

            // 3. 如果都获取失败，使用备选方案（机器名 + 用户名 + 随机数种子）
            if (identifiers.isEmpty()) {
                String fallback = getFallbackIdentifier();
                identifiers.add(fallback);
                log.warn("[MachineId] 未能获取MAC/序列号，使用备选标识: {}", fallback);
            }

            // 4. 组合并哈希
            String combined = String.join("|", identifiers);
            return md5(combined).substring(0, 16);

        } catch (Exception e) {
            log.error("[MachineId] 生成机器标识失败: {}", e.getMessage());
            // 兜底：使用随机标识（不理想，但避免系统崩溃）
            return "fallback-" + System.currentTimeMillis();
        }
    }

    /**
     * 获取 MAC 地址
     * 优先返回局域网 MAC（不是 127.0.0.1 对应的）
     */
    private static String getMacAddress() {
        try {
            List<String> macs = new ArrayList<>();
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
                    continue;
                }
                byte[] mac = ni.getHardwareAddress();
                if (mac == null || mac.length == 0) {
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                String macStr = sb.toString();
                // 跳过 localhost 的 MAC
                if (!macStr.equals("00-00-00-00-00-00")) {
                    macs.add(macStr);
                }
            }

            if (!macs.isEmpty()) {
                // 返回第一个有效的 MAC（通常是有线网卡或主要无线网卡）
                return macs.getFirst();
            }
        } catch (Exception e) {
            log.debug("[MachineId] 获取MAC地址失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取机器序列号（Windows: BIOS序列号, Linux: 产品UUID, Mac: 平台UUID）
     */
    private static String getMachineSerialNumber() {
        String os = System.getProperty("os.name", "").toLowerCase();

        try {
            ProcessBuilder pb;
            if (os.contains("win")) {
                // Windows: 使用 wmic 获取 BIOS 序列号
                pb = new ProcessBuilder("wmic", "bios", "get", "serialnumber");
            } else if (os.contains("mac") || os.contains("darwin")) {
                // Mac: 使用 system_profiler 获取平台 UUID
                pb = new ProcessBuilder("system_profiler", "SPPlatformHardwareDataType");
            } else {
                // Linux: 尝试读取 /sys/class/dmi/id/product_uuid
                pb = new ProcessBuilder("cat", "/sys/class/dmi/id/product_uuid");
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line.trim());
                }

                process.waitFor();
                String result = output.toString().trim();

                // 过滤无效值
                if (!result.isEmpty()
                        && !result.contains("To be filled")
                        && !result.contains("Serial Number")
                        && result.length() > 4) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug("[MachineId] 获取机器序列号失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 备选标识（当 MAC 和序列号都无法获取时）
     * 使用机器名 + 用户名 + JVM 进程 ID 的组合
     */
    private static String getFallbackIdentifier() {
        String machineName = System.getProperty("computerName",
                System.getProperty("host.name", "unknown"));
        String userName = System.getProperty("user.name", "unknown");
        long processId = ProcessHandle.current().pid();
        return machineName + "-" + userName + "-" + processId;
    }

    /**
     * MD5 哈希
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // MD5 不会失败，抛出说明 JVM 配置有问题
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
