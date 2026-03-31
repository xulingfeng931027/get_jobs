package com.getjobs.worker.utils;

import com.getjobs.application.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.fluent.Request;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author loks666
 * 项目链接: <a href="https://github.com/loks666/get_jobs">https://github.com/loks666/get_jobs</a>
 */
@Slf4j
@Service
public class Bot {

    private static volatile Bot INSTANCE;

    private final ConfigService configService;
    private String hookUrl;
    private boolean isSend;
    private String dingtalkHookUrl;
    private boolean dingtalkIsSend;

    /**
     * 投递记录数据结构
     */
    private static class DeliveryRecord {
        final String platform;
        final String companyName;
        final String jobName;

        DeliveryRecord(String platform, String companyName, String jobName) {
            this.platform = platform;
            this.companyName = companyName;
            this.jobName = jobName;
        }
    }

    /**
     * 投递记录缓冲区，使用 CopyOnWriteArrayList 保证线程安全
     */
    private static final List<DeliveryRecord> deliveryBuffer = new CopyOnWriteArrayList<>();

    @Autowired
    public Bot(ConfigService configService) {
        this.configService = configService;
        INSTANCE = this;
        reloadConfig();
    }

    /**
     * 从数据库配置表加载所需配置
     */
    public void reloadConfig() {
        try {
            // 企业微信配置
            this.hookUrl = configService.getConfigValue("HOOK_URL");
            String sendFlag = configService.getConfigValue("BOT_IS_SEND");
            this.isSend = ("true".equalsIgnoreCase(sendFlag) || "1".equals(sendFlag));

            if (this.hookUrl == null || this.hookUrl.isBlank()) {
                log.warn("HOOK_URL 未配置，企业微信 Bot 将不发送消息。");
                this.isSend = false;
            }
        } catch (Exception e) {
            log.error("加载企业微信 Bot 配置失败: {}", e.getMessage());
            this.isSend = false;
        }

        try {
            // 钉钉配置
            this.dingtalkHookUrl = configService.getConfigValue("DINGTALK_HOOK_URL");
            String dingtalkSendFlag = configService.getConfigValue("DINGTALK_IS_SEND");
            this.dingtalkIsSend = ("true".equalsIgnoreCase(dingtalkSendFlag) || "1".equals(dingtalkSendFlag));

            if (this.dingtalkHookUrl == null || this.dingtalkHookUrl.isBlank()) {
                log.warn("DINGTALK_HOOK_URL 未配置，钉钉 Bot 将不发送消息。");
                this.dingtalkIsSend = false;
            }
        } catch (Exception e) {
            log.error("加载钉钉 Bot 配置失败: {}", e.getMessage());
            this.dingtalkIsSend = false;
        }
    }

    public static void sendMessageByTime(String message) {
        Bot inst = INSTANCE;
        if (inst == null) {
            log.error("Bot 尚未初始化为 Spring Bean，忽略发送。");
            return;
        }
        inst.sendMessageByTimeInstance(message);
    }

    public void sendMessageByTimeInstance(String message) {
        if (!isSend && !dingtalkIsSend) {
            return;
        }
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formattedMessage = String.format("%s %s", currentTime, message);
        sendMessageInstance(formattedMessage);
    }

    public static void sendMessage(String message) {
        Bot inst = INSTANCE;
        if (inst == null) {
            log.warn("Bot 尚未初始化为 Spring Bean，忽略发送。");
            return;
        }
        inst.sendMessageInstance(message);
    }

    public void sendMessageInstance(String message) {
        // 企业微信推送
        if (isSend) {
            if (hookUrl == null || hookUrl.isBlank()) {
                log.warn("HOOK_URL 未设置，无法推送企业微信消息。");
            } else {
                try {
                    String response = Request.post(hookUrl)
                            .bodyString("{\"msgtype\": \"text\", \"text\": {\"content\": \"" + message + "\"}}",
                                    org.apache.hc.core5.http.ContentType.APPLICATION_JSON)
                            .execute()
                            .returnContent()
                            .asString();
                    log.info("企业微信消息推送成功: {}", response);
                } catch (Exception e) {
                    log.error("企业微信消息推送失败: {}", e.getMessage());
                }
            }
        }

        // 钉钉推送
        if (dingtalkIsSend) {
            if (dingtalkHookUrl == null || dingtalkHookUrl.isBlank()) {
                log.warn("DINGTALK_HOOK_URL 未设置，无法推送钉钉消息。");
            } else {
                try {
                    String response = Request.post(dingtalkHookUrl)
                            .bodyString("{\"msgtype\": \"text\", \"text\": {\"content\": \"" + message + "\"}}",
                                    org.apache.hc.core5.http.ContentType.APPLICATION_JSON)
                            .execute()
                            .returnContent()
                            .asString();
                    log.info("钉钉消息推送成功: {}", response);
                } catch (Exception e) {
                    log.error("钉钉消息推送失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 记录投递信息到缓冲区
     * 静态方法，供各 worker 调用
     *
     * @param platform    平台名
     * @param companyName 公司名
     * @param jobName     岗位名
     */
    public static void recordDelivery(String platform, String companyName, String jobName) {
        Bot inst = INSTANCE;
        // 如果企业微信和钉钉都关闭了，不需要记录投递（节省内存）
        if (inst == null || (!inst.isSend && !inst.dingtalkIsSend)) {
            return;
        }
        deliveryBuffer.add(new DeliveryRecord(platform, companyName, jobName));
        log.debug("记录投递: [{}] {} | {}", platform, companyName, jobName);
    }

    /**
     * 定时聚合推送投递记录
     * 每5分钟（300000毫秒）执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledDeliverySummary() {
        // 如果缓冲区为空，跳过
        if (deliveryBuffer.isEmpty()) {
            return;
        }

        // 取出所有记录并清空缓冲区
        List<DeliveryRecord> records = new ArrayList<>(deliveryBuffer);
        deliveryBuffer.clear();

        // 生成汇总消息
        String message = formatDeliverySummary(records);

        // 发送汇总消息
        sendMessageInstance(message);
        log.info("投递汇总消息已发送，共 {} 条记录", records.size());
    }

    /**
     * 格式化投递汇总消息
     *
     * @param records 投递记录列表
     * @return 格式化后的消息
     */
    private String formatDeliverySummary(List<DeliveryRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 投递汇总（最近5分钟）\n\n");
        sb.append("共投递 ").append(records.size()).append(" 个岗位：\n");

        // 按平台分组
        Map<String, List<DeliveryRecord>> groupedByPlatform = records.stream()
                .collect(Collectors.groupingBy(r -> r.platform, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<DeliveryRecord>> entry : groupedByPlatform.entrySet()) {
            sb.append("\n【").append(entry.getKey()).append("】\n");
            for (DeliveryRecord record : entry.getValue()) {
                sb.append("- ").append(record.companyName).append(" | ").append(record.jobName).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public static void main(String[] args) {
        // 本地测试请确保 Spring 容器已初始化并注入 ConfigService。
    }

}
