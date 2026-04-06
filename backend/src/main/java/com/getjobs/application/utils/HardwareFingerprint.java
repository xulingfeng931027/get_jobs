package com.getjobs.application.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 硬件指纹工具
 * 通过 CPU 序列号、主板序列号、MAC 地址生成唯一机器标识
 */
@Slf4j
public class HardwareFingerprint {

    private static final String UNKNOWN = "UNKNOWN";

    /**
     * 获取机器唯一标识
     * 结合 CPU 序列、主板序列、MAC 地址生成
     */
    public static String getMachineId() {
        try {
            String cpuSerial = getCPUId();
            String mbSerial = getMotherboardSerial();
            String mac = getFirstMacAddress();

            String raw = String.join("|", cpuSerial, mbSerial, mac);
            log.debug("机器指纹原始值: CPU={}, MB={}, MAC={}", cpuSerial, mbSerial, mac);

            return UUID.nameUUIDFromBytes(raw.getBytes()).toString();
        } catch (Exception e) {
            log.error("获取机器指纹失败", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 获取 CPU 序列号
     */
    public static String getCPUId() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;

            if (os.contains("windows")) {
                command = "wmic cpu get ProcessorID";
            } else if (os.contains("linux")) {
                command = "cat /proc/cpuinfo | grep -i 'serial' | head -1";
            } else if (os.contains("mac")) {
                command = "system_profiler SPHardwareDataType | grep 'Hardware UUID'";
            } else {
                return generateFallbackId("cpu");
            }

            String result = executeCommand(command);
            return parseCpuId(result, os);

        } catch (Exception e) {
            log.warn("获取 CPU ID 失败: {}", e.getMessage());
            return generateFallbackId("cpu");
        }
    }

    /**
     * 获取主板序列号
     */
    public static String getMotherboardSerial() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String command;

            if (os.contains("windows")) {
                command = "wmic baseboard get SerialNumber";
            } else if (os.contains("linux")) {
                command = "cat /sys/class/dmi/id/board_serial";
            } else if (os.contains("mac")) {
                command = "system_profiler SPHardwareDataType | grep 'AppleSerialNumber'";
            } else {
                return generateFallbackId("mb");
            }

            String result = executeCommand(command);
            return cleanSerialNumber(result);

        } catch (Exception e) {
            log.warn("获取主板序列号失败: {}", e.getMessage());
            return generateFallbackId("mb");
        }
    }

    /**
     * 获取第一个 MAC 地址
     */
    public static String getFirstMacAddress() {
        try {
            return NetworkInterface.networkInterfaces()
                .collect(Collectors.toList())
                .stream()
                .filter(ni -> {
                    try {
                        return !ni.isLoopback() && ni.isUp();
                    } catch (SocketException e) {
                        return false;
                    }
                })
                .findFirst()
                .map(ni -> {
                    try {
                        byte[] mac = ni.getHardwareAddress();
                        if (mac == null || mac.length == 0) {
                            return UNKNOWN;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (byte b : mac) {
                            sb.append(String.format("%02X", b));
                        }
                        return sb.toString();
                    } catch (SocketException e) {
                        return UNKNOWN;
                    }
                })
                .orElse(UNKNOWN);

        } catch (Exception e) {
            log.warn("获取 MAC 地址失败: {}", e.getMessage());
            return generateFallbackId("mac");
        }
    }

    private static String executeCommand(String command) throws Exception {
        ProcessBuilder pb;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            pb = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            pb = new ProcessBuilder("/bin/sh", "-c", command);
        }

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
        }

        process.waitFor();
        return result.toString();
    }

    private static String parseCpuId(String raw, String os) {
        if (raw == null || raw.isEmpty()) {
            return generateFallbackId("cpu");
        }

        String[] lines = raw.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Windows: 通常第二行是 ProcessorID
            if (os.contains("windows")) {
                if (line.contains("ProcessorID")) continue;
                if (line.length() > 8) {
                    return cleanSerialNumber(line);
                }
            } else {
                // Linux/Mac: 提取值
                if (line.contains(":")) {
                    String value = line.substring(line.indexOf(":") + 1).trim();
                    if (!value.isEmpty() && !value.equals("0")) {
                        return cleanSerialNumber(value);
                    }
                } else if (line.length() > 8) {
                    return cleanSerialNumber(line);
                }
            }
        }

        return generateFallbackId("cpu");
    }

    private static String cleanSerialNumber(String raw) {
        if (raw == null) return UNKNOWN;

        // 移除换行、多余空格
        String cleaned = raw.trim()
            .replaceAll("[\n\r\t]", " ")
            .replaceAll("\\s+", " ");

        // 移除常见前缀
        cleaned = cleaned.replaceAll("^(SerialNumber|ProcessorID|UUID|Serial)\\s*[:=]?\\s*", "");

        // 移除非打印字符
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9\\-]", "");

        return cleaned.isEmpty() ? UNKNOWN : cleaned;
    }

    private static String generateFallbackId(String prefix) {
        // 基于系统属性生成伪随机但稳定的 ID
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("-");
        sb.append(System.getProperty("user.name", "default"));
        sb.append("-");
        sb.append(System.getProperty("user.dir", "/tmp").hashCode());
        return UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString().substring(0, 12);
    }

    /**
     * 验证机器 ID 是否匹配
     */
    public static boolean validateMachineId(String expectedMachineId) {
        String currentMachineId = getMachineId();
        return currentMachineId.equals(expectedMachineId);
    }
}
