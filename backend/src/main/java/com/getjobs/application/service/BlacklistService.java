package com.getjobs.application.service;

import com.getjobs.application.entity.CommonOptionEntity;
import com.getjobs.application.mapper.CommonOptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共黑名单服务
 * 从 common_option 表（type='blacklist'）读取黑名单关键词
 * 对职位名称和公司名进行模糊匹配过滤
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final CommonOptionMapper commonOptionMapper;

    /**
     * 获取所有黑名单关键词
     * 查询 common_option 表中 type='blacklist' 的所有记录
     *
     * @return 黑名单关键词列表
     */
    public List<String> getBlacklistKeywords() {
        List<CommonOptionEntity> options = commonOptionMapper.selectByType("blacklist");
        return options.stream()
                .map(CommonOptionEntity::getValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 检查职位是否命中黑名单
     *
     * @param jobTitle    职位名称
     * @param companyName 公司名称
     * @return true 如果命中黑名单（应跳过）
     */
    public boolean isBlacklisted(String jobTitle, String companyName) {
        List<String> keywords = getBlacklistKeywords();
        if (keywords.isEmpty()) {
            return false;
        }

        // 将职位名称和公司名称统一转为小写进行忽略大小写的匹配
        String lowerJobTitle = jobTitle != null ? jobTitle.toLowerCase() : "";
        String lowerCompanyName = companyName != null ? companyName.toLowerCase() : "";

        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            String lowerKeyword = keyword.toLowerCase();

            // 模糊匹配：包含即命中
            if (lowerJobTitle.contains(lowerKeyword) || lowerCompanyName.contains(lowerKeyword)) {
                log.debug("黑名单命中：关键词【{}】匹配到 职位【{}】或 公司【{}】", keyword, jobTitle, companyName);
                return true;
            }
        }
        return false;
    }

    /**
     * 检查职位是否命中黑名单，并返回命中的关键词
     *
     * @param jobTitle    职位名称
     * @param companyName 公司名称
     * @return 命中的关键词，未命中返回 null
     */
    public String findMatchedBlacklistKeyword(String jobTitle, String companyName) {
        List<String> keywords = getBlacklistKeywords();
        if (keywords.isEmpty()) {
            return null;
        }

        String lowerJobTitle = jobTitle != null ? jobTitle.toLowerCase() : "";
        String lowerCompanyName = companyName != null ? companyName.toLowerCase() : "";

        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            String lowerKeyword = keyword.toLowerCase();

            if (lowerJobTitle.contains(lowerKeyword) || lowerCompanyName.contains(lowerKeyword)) {
                return keyword;
            }
        }
        return null;
    }
}
