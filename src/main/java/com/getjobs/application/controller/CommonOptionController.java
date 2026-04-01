package com.getjobs.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.getjobs.application.entity.CommonOptionEntity;
import com.getjobs.application.mapper.CommonOptionMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/common-option")
public class CommonOptionController {

    private final CommonOptionMapper commonOptionMapper;
    private final DataSource dataSource;

    public CommonOptionController(CommonOptionMapper commonOptionMapper, DataSource dataSource) {
        this.commonOptionMapper = commonOptionMapper;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void ensureTableExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS common_option (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " type VARCHAR(50) NOT NULL," +
                " label VARCHAR(200) NOT NULL," +
                " value TEXT NOT NULL," +
                " sort_order INTEGER DEFAULT 0," +
                " created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                " updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            log.info("确保 common_option 表已存在");
        } catch (Exception e) {
            log.warn("创建 common_option 表失败: {}", e.getMessage());
        }
    }

    /**
     * 获取所有选项，支持按 type 过滤，按 sort_order 排序
     */
    @GetMapping
    public List<CommonOptionEntity> getAllOptions(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            return commonOptionMapper.selectByType(type);
        }
        QueryWrapper<CommonOptionEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order");
        return commonOptionMapper.selectList(wrapper);
    }

    /**
     * 新增单个选项
     */
    @PostMapping
    public CommonOptionEntity createOption(@RequestBody CommonOptionEntity option) {
        LocalDateTime now = LocalDateTime.now();
        option.setId(null);
        option.setCreatedAt(now);
        option.setUpdatedAt(now);
        if (option.getSortOrder() == null) {
            option.setSortOrder(0);
        }
        commonOptionMapper.insert(option);
        return option;
    }

    /**
     * 批量新增选项
     */
    @PostMapping("/batch")
    public List<CommonOptionEntity> createBatchOptions(@RequestBody List<CommonOptionEntity> options) {
        LocalDateTime now = LocalDateTime.now();
        for (CommonOptionEntity option : options) {
            option.setId(null);
            option.setCreatedAt(now);
            option.setUpdatedAt(now);
            if (option.getSortOrder() == null) {
                option.setSortOrder(0);
            }
            commonOptionMapper.insert(option);
        }
        return options;
    }

    /**
     * 更新选项
     */
    @PutMapping("/{id}")
    public CommonOptionEntity updateOption(@PathVariable Long id, @RequestBody CommonOptionEntity option) {
        option.setId(id);
        option.setUpdatedAt(LocalDateTime.now());
        commonOptionMapper.updateById(option);
        return commonOptionMapper.selectById(id);
    }

    /**
     * 删除选项
     */
    @DeleteMapping("/{id}")
    public boolean deleteOption(@PathVariable Long id) {
        return commonOptionMapper.deleteById(id) > 0;
    }

    /**
     * 批量更新排序
     * 接收格式: [{"id": 1, "sortOrder": 0}, {"id": 2, "sortOrder": 1}]
     */
    @PutMapping("/sort")
    public boolean updateSort(@RequestBody List<Map<String, Object>> sortList) {
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> item : sortList) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sortOrder = Integer.valueOf(item.get("sortOrder").toString());
            CommonOptionEntity option = new CommonOptionEntity();
            option.setId(id);
            option.setSortOrder(sortOrder);
            option.setUpdatedAt(now);
            commonOptionMapper.updateById(option);
        }
        return true;
    }
}
