package com.getjobs.application.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.getjobs.application.entity.SearchPresetEntity;
import com.getjobs.application.mapper.SearchPresetMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/search-preset")
public class SearchPresetController {

    private final SearchPresetMapper searchPresetMapper;
    private final DataSource dataSource;

    public SearchPresetController(SearchPresetMapper searchPresetMapper, DataSource dataSource) {
        this.searchPresetMapper = searchPresetMapper;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void ensureTableExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS search_preset (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR(200)," +
                " keywords TEXT," +
                " city VARCHAR(100)," +
                " created_at DATETIME," +
                " updated_at DATETIME" +
                ")";
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            log.info("确保 search_preset 表已存在");
        } catch (Exception e) {
            log.warn("创建 search_preset 表失败: {}", e.getMessage());
        }
    }

    /**
     * 获取所有预设列表（按 updatedAt 倒序）
     */
    @GetMapping
    public List<SearchPresetEntity> getAllPresets() {
        QueryWrapper<SearchPresetEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("updated_at");
        return searchPresetMapper.selectList(wrapper);
    }

    /**
     * 创建新预设
     */
    @PostMapping
    public SearchPresetEntity createPreset(@RequestBody SearchPresetEntity preset) {
        LocalDateTime now = LocalDateTime.now();
        preset.setId(null);
        preset.setCreatedAt(now);
        preset.setUpdatedAt(now);
        searchPresetMapper.insert(preset);
        return preset;
    }

    /**
     * 更新预设
     */
    @PutMapping("/{id}")
    public SearchPresetEntity updatePreset(@PathVariable Long id, @RequestBody SearchPresetEntity preset) {
        preset.setId(id);
        preset.setUpdatedAt(LocalDateTime.now());
        searchPresetMapper.updateById(preset);
        return searchPresetMapper.selectById(id);
    }

    /**
     * 删除预设
     */
    @DeleteMapping("/{id}")
    public boolean deletePreset(@PathVariable Long id) {
        return searchPresetMapper.deleteById(id) > 0;
    }
}
