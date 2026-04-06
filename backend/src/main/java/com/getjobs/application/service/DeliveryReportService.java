package com.getjobs.application.service;

import com.getjobs.application.entity.DeliveryReportEntity;
import com.getjobs.application.mapper.DeliveryReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 投递报告服务
 * 处理客户端投递结果上报的存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryReportService {

    private final DeliveryReportMapper deliveryReportMapper;

    /**
     * 保存投递报告
     *
     * @param userId 用户ID
     * @param platform 平台 (boss/liepin/job51/zhilian)
     * @param deliveredJobs 成功投递的职位
     * @param filteredJobs 被过滤的职位
     * @param failedJobs 投递失败的职位
     * @param totalCount 总计处理数
     * @param deviceId 设备ID
     * @param clientVersion 客户端版本
     * @return 报告结果 {reportId, receivedCount}
     */
    @Transactional
    public Map<String, Object> saveDeliveryReport(
            Long userId,
            String platform,
            Map<String, Object> deliveredJobs,
            Map<String, Object> filteredJobs,
            Map<String, Object> failedJobs,
            Integer totalCount,
            String deviceId,
            String clientVersion) {

        // 生成报告ID
        String reportId = "rpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 创建报告实体
        DeliveryReportEntity report = new DeliveryReportEntity();
        report.setReportId(reportId);
        report.setUserId(userId);
        report.setPlatform(platform);
        report.setDeliveredCount(deliveredJobs != null ? deliveredJobs.size() : 0);
        report.setFilteredCount(filteredJobs != null ? filteredJobs.size() : 0);
        report.setFailedCount(failedJobs != null ? failedJobs.size() : 0);
        report.setTotalCount(totalCount != null ? totalCount : 0);
        report.setDeliveredJobs(deliveredJobs != null ? convertMapToJson(deliveredJobs) : null);
        report.setFilteredJobs(filteredJobs != null ? convertMapToJson(filteredJobs) : null);
        report.setFailedJobs(failedJobs != null ? convertMapToJson(failedJobs) : null);
        report.setDeviceId(deviceId);
        report.setClientVersion(clientVersion);
        report.setCreatedAt(LocalDateTime.now());

        // 保存报告
        deliveryReportMapper.insert(report);

        log.info("[投递报告] 保存报告成功: reportId={}, userId={}, platform={}, delivered={}",
                reportId, userId, platform, report.getDeliveredCount());

        return Map.of(
                "reportId", reportId,
                "receivedCount", report.getDeliveredCount() + report.getFilteredCount() + report.getFailedCount()
        );
    }

    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(((String) value).replace("\"", "\\\"")).append("\"");
            } else {
                sb.append(value);
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}