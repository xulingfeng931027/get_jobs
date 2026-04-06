// 从 SQLite 读取数据并生成 MySQL INSERT 语句
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import initSqlJs from 'sql.js';
import { createRequire } from 'module';

const log = (...args) => console.log('[export-sqlite]', ...args);

async function main() {
  try {
    const __filename = fileURLToPath(import.meta.url);
    const __dirname = path.dirname(__filename);
    const projectRoot = path.resolve(__dirname, '..');
    const dbPath = path.resolve(projectRoot, 'getjobs.db');
    const outputFile = path.resolve(projectRoot, 'db', 'insert_data.sql');

    if (!fs.existsSync(dbPath)) {
      throw new Error(`Database file not found: ${dbPath}`);
    }

    log('Loading database:', dbPath);
    
    const require = createRequire(import.meta.url);
    const wasmDir = path.dirname(require.resolve('sql.js/dist/sql-wasm.wasm'));
    const SQL = await initSqlJs({ locateFile: (file) => path.join(wasmDir, file) });

    const fileBuffer = fs.readFileSync(dbPath);
    const u8 = new Uint8Array(fileBuffer);
    const db = new SQL.Database(u8);

    // 获取所有表名
    const tablesResult = db.exec("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;");
    if (!tablesResult || tablesResult.length === 0) {
      throw new Error('No tables found in database');
    }

    const tables = tablesResult[0].values.map(row => row[0]);
    log(`Found ${tables.length} tables:`, tables);

    // 生成 SQL 文件
    let sqlContent = '-- =====================================================\n';
    sqlContent += '-- Get Jobs MySQL 数据初始化脚本\n';
    sqlContent += `-- 生成时间: ${new Date().toISOString()}\n`;
    sqlContent += '-- 说明: 从 SQLite 数据库导出的数据\n';
    sqlContent += '-- =====================================================\n\n';
    sqlContent += 'USE `get_jobs`;\n\n';

    for (const tableName of tables) {
      log(`\nProcessing table: ${tableName}`);
      
      // 获取表结构
      const pragmaResult = db.exec(`PRAGMA table_info(${tableName});`);
      if (!pragmaResult || pragmaResult.length === 0) {
        log(`  Warning: Could not get schema for ${tableName}`);
        continue;
      }

      const columns = pragmaResult[0].values.map(row => row[1]);
      log(`  Columns: ${columns.join(', ')}`);

      // 获取数据行数
      const countResult = db.exec(`SELECT COUNT(*) FROM ${tableName};`);
      const rowCount = countResult[0].values[0][0];
      log(`  Row count: ${rowCount}`);

      if (rowCount === 0) {
        sqlContent += `-- 表 ${tableName} 没有数据\n\n`;
        continue;
      }

      // 注释掉 TRUNCATE
      sqlContent += `-- TRUNCATE TABLE \`${tableName}\`;\n`;

      // 获取所有数据
      const dataResult = db.exec(`SELECT * FROM ${tableName};`);
      if (!dataResult || dataResult.length === 0) {
        continue;
      }

      const rows = dataResult[0].values;
      let insertCount = 0;

      for (const row of rows) {
        try {
          // 构建 VALUES
          const values = row.map(val => {
            if (val === null) {
              return 'NULL';
            } else if (typeof val === 'string') {
              // 转义特殊字符
              const escaped = val
                .replace(/\\/g, '\\\\')
                .replace(/'/g, "\\'")
                .replace(/\n/g, '\\n')
                .replace(/\r/g, '\\r');
              return `'${escaped}'`;
            } else if (typeof val === 'number') {
              return val.toString();
            } else {
              return `'${String(val).replace(/'/g, "''")}'`;
            }
          });

          const columnsStr = columns.map(col => `\`${col}\``).join(', ');
          const valuesStr = values.join(', ');
          
          sqlContent += `INSERT INTO \`${tableName}\` (${columnsStr}) VALUES (${valuesStr});\n`;
          insertCount++;
        } catch (err) {
          log(`  Warning: Error processing row:`, err.message);
          sqlContent += `-- 错误: 跳过一行数据 - ${err.message}\n`;
        }
      }

      sqlContent += `\n-- 表 ${tableName}: 插入 ${insertCount} 条记录\n\n`;
      log(`  ✓ Generated ${insertCount} INSERT statements`);
    }

    // 写入文件
    fs.writeFileSync(outputFile, sqlContent, 'utf-8');
    log(`\n✓ SQL file saved to: ${outputFile}`);
    log(`Total tables processed: ${tables.length}`);

    db.close();
    log('\nExport completed successfully!');
  } catch (err) {
    console.error('[export-sqlite] Export failed:', err);
    process.exitCode = 1;
  }
}

await main();
