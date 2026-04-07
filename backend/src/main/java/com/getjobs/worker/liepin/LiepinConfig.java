package com.getjobs.worker.liepin;

import lombok.Data;

import java.util.List;

/**
 * @author xulingfeng
 * 项目链接: <a href="https://github.com/xulingfeng/get_jobs">https://github.com/xulingfeng/get_jobs</a>
 */
@Data
public class LiepinConfig {
    /**
     * 搜索关键词列表
     */
    private List<String> keywords;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 薪资范围
     */
    private String salary;

}
