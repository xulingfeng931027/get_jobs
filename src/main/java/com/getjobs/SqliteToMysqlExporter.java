package com.getjobs;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SQLite 到 MySQL 数据迁移工具
 * 读取 getjobs.db (SQLite) 并生成 MySQL INSERT 语句
 */
public class SqliteToMysqlExporter {
    
    private static final String SQLITE_DB_PATH = "E:\\code\\get_jobs\\getjobs.db";
    private static final String OUTPUT_SQL_PATH = "E:\\code\\get_jobs\\db\\insert_data.sql";
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("SQLite to MySQL Data Exporter");
        System.out.println("========================================\n");
        
        try {
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");
            
            // 连接 SQLite 数据库
            System.out.println("Connecting to SQLite database: " + SQLITE_DB_PATH);
            Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + SQLITE_DB_PATH);
            System.out.println("✓ Connected successfully\n");
            
            // 获取所有表名
            List<String> tables = getAllTables(sqliteConn);
            System.out.println("Found " + tables.size() + " tables: " + tables + "\n");
            
            // 生成 SQL 文件
            generateMySqlInsertStatements(sqliteConn, tables);
            
            // 关闭连接
            sqliteConn.close();
            
            System.out.println("\n========================================");
            System.out.println("Export completed successfully!");
            System.out.println("SQL file saved to: " + OUTPUT_SQL_PATH);
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Error during export: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static List<String> getAllTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }
        
        return tables;
    }
    
    private static void generateMySqlInsertStatements(Connection sqliteConn, List<String> tables) 
            throws SQLException, IOException {
        
        StringBuilder sqlContent = new StringBuilder();
        
        // 文件头
        sqlContent.append("-- =====================================================\n");
        sqlContent.append("-- Get Jobs MySQL 数据初始化脚本\n");
        sqlContent.append("-- 生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sqlContent.append("-- 说明: 从 SQLite 数据库导出的数据\n");
        sqlContent.append("-- =====================================================\n\n");
        sqlContent.append("USE `get_jobs`;\n\n");
        
        int totalRecords = 0;
        
        for (String tableName : tables) {
            System.out.println("Processing table: " + tableName);
            
            // 获取表结构
            List<String> columns = getTableColumns(sqliteConn, tableName);
            System.out.println("  Columns: " + String.join(", ", columns));
            
            // 获取数据行数
            int rowCount = getRowCount(sqliteConn, tableName);
            System.out.println("  Row count: " + rowCount);
            
            if (rowCount == 0) {
                sqlContent.append("-- 表 ").append(tableName).append(" 没有数据\n\n");
                continue;
            }
            
            // 注释掉 TRUNCATE
            sqlContent.append("-- TRUNCATE TABLE `").append(tableName).append("`;\n");
            
            // 获取所有数据并生成 INSERT 语句
            int insertCount = generateInsertStatements(sqliteConn, tableName, columns, sqlContent);
            totalRecords += insertCount;
            
            sqlContent.append("\n-- 表 ").append(tableName).append(": 插入 ").append(insertCount).append(" 条记录\n\n");
            System.out.println("  ✓ Generated " + insertCount + " INSERT statements\n");
        }
        
        // 写入文件
        Files.writeString(Path.of(OUTPUT_SQL_PATH), sqlContent.toString());
        System.out.println("Total records exported: " + totalRecords);
    }
    
    private static List<String> getTableColumns(Connection conn, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        String sql = "PRAGMA table_info(" + tableName + ")";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        
        return columns;
    }
    
    private static int getRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    private static int generateInsertStatements(Connection conn, String tableName, 
                                                List<String> columns, StringBuilder output) 
            throws SQLException {
        
        String sql = "SELECT * FROM " + tableName;
        int count = 0;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                StringBuilder insertStmt = new StringBuilder();
                insertStmt.append("INSERT INTO `").append(tableName).append("` (");
                
                // 列名
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) insertStmt.append(", ");
                    insertStmt.append("`").append(metaData.getColumnName(i)).append("`");
                }
                
                insertStmt.append(") VALUES (");
                
                // 值
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) insertStmt.append(", ");
                    
                    Object value = rs.getObject(i);
                    if (value == null || rs.wasNull()) {
                        insertStmt.append("NULL");
                    } else if (value instanceof String) {
                        // 转义特殊字符
                        String escaped = escapeString((String) value);
                        insertStmt.append("'").append(escaped).append("'");
                    } else if (value instanceof Number) {
                        insertStmt.append(value);
                    } else {
                        // 其他类型转为字符串
                        String escaped = escapeString(value.toString());
                        insertStmt.append("'").append(escaped).append("'");
                    }
                }
                
                insertStmt.append(");\n");
                output.append(insertStmt);
                count++;
            }
        }
        
        return count;
    }
    
    private static String escapeString(String str) {
        if (str == null) return "";
        
        return str.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
