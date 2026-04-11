/**
 * 前端代码混淆脚本 v2.0
 * 在 Next.js 构建后对输出文件进行混淆处理
 * 优化版本：更好地处理 Next.js 特殊文件
 */

const JavaScriptObfuscator = require('javascript-obfuscator');
const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
const outputDir = args[0] || path.join(__dirname, '..', 'out');

// 混淆统计
let stats = {
  processed: 0,
  skipped: 0,
  failed: 0,
};

// 混淆配置 - 针对 Next.js 优化
const obfuscatorOptions = {
  // 代码压缩
  compact: true,

  // 控制流扁平化（影响性能，适度使用）
  controlFlowFlattening: false, // Next.js chunks 禁用，避免破坏执行顺序

  // 死代码注入
  deadCodeInjection: false, // 增加体积，不适合生产

  // 调试保护
  debugProtection: false,

  // 标识符混淆
  identifierNamesGenerator: 'hexadecimal',
  identifiersPrefix: '_',

  // 保留标识符（Next.js/React 必需）
  reservedNames: [
    // Next.js 运行时
    '^next',
    '^NEXT',
    'next',
    'Next',
    'NEXT_DATA',
    '__NEXT_DATA__',
    '__NEXT_LOADED_PAGES__',
    '__NEXT_LOADED_WIDGETS__',
    '__NEXT_REGISTER_STYLE__',
    // React
    'React',
    'react',
    'createElement',
    'Component',
    'Fragment',
    // 全局对象
    'window',
    'document',
    'location',
    'navigator',
    'history',
    'localStorage',
    'sessionStorage',
    'fetch',
    'Promise',
    'Map',
    'Set',
    'Array',
    'Object',
    'String',
    'Number',
    'Boolean',
    'undefined',
    'null',
    // 页面组件
    'page',
    'props',
    'state',
    'query',
    'params',
    'pathname',
    'buildId',
    'assetPrefix',
    'runtimeConfig',
    'nextExport',
    'isFallback',
    'gip',
    'appGip',
    '__NUXT__',
    '__nuxt',
    // 事件
    'onClick',
    'onChange',
    'onSubmit',
    'onKeyDown',
    'onKeyUp',
    'onFocus',
    'onBlur',
  ],

  // 字符串数组（核心保护）
  stringArray: true,
  stringArrayEncoding: ['base64'],
  stringArrayThreshold: 0.8,

  // 对象键转换
  transformObjectKeys: false, // 可能破坏 CSS-in-JS

  // 变量名混淆
  renameGlobals: false,
  renameProperties: false,

  // 跳过模块导出名称
  forceComputedKeys: false,

  // 死代码
  selfDefending: false, // 与压缩冲突

  // 源码映射
  sourceMap: false,
  sourceMapMode: 'separate',
};

/**
 * 检查文件是否应该被混淆
 */
function shouldObfuscate(filePath) {
  const stat = fs.statSync(filePath);

  // 只处理 JS 文件
  if (!filePath.endsWith('.js')) {
    return false;
  }

  // 跳过已混淆文件
  if (filePath.includes('.obfuscated.')) {
    return false;
  }

  // 跳过 chunk 文件（Next.js 特殊处理）
  // 这些文件名是哈希，混淆后无法正确加载
  const fileName = path.basename(filePath);
  if (/^[a-f0-9]{8,}\.js$/.test(fileName.replace('.js', ''))) {
    console.log(`  ⏭️  跳过 chunk 文件: ${path.relative(outputDir, filePath)}`);
    return false;
  }

  // 跳过 manifest 文件
  if (fileName.includes('manifest') || fileName.includes('MANIFEST')) {
    return false;
  }

  return true;
}

/**
 * 递归处理目录中的所有 JS 文件
 */
function obfuscateDirectory(dirPath, relativePath = '') {
  try {
    const files = fs.readdirSync(dirPath);

    files.forEach(file => {
      const filePath = path.join(dirPath, file);
      const fileRelativePath = path.join(relativePath, file);
      const stat = fs.statSync(filePath);

      if (stat.isDirectory()) {
        // 跳过特定目录
        if (file === 'node_modules' || file === '.next' || file === 'chunks') {
          return;
        }
        // 跳过 _next/static 中的某些目录
        if (fileRelativePath.includes('_next/static')) {
          // static 目录下的 chunks 保持原样
          if (file === 'chunks') {
            return;
          }
        }
        obfuscateDirectory(filePath, fileRelativePath);
      } else if (shouldObfuscate(filePath)) {
        obfuscateFile(filePath);
      }
    });
  } catch (error) {
    console.error(`  ❌ 目录读取失败: ${dirPath}`, error.message);
    stats.failed++;
  }
}

/**
 * 混淆单个 JS 文件
 */
function obfuscateFile(filePath) {
  try {
    const code = fs.readFileSync(filePath, 'utf8');

    // 跳过太小的文件
    if (code.length < 200) {
      console.log(`  ⏭️  跳过小文件: ${path.relative(outputDir, filePath)}`);
      stats.skipped++;
      return;
    }

    const relativePath = path.relative(outputDir, filePath);
    console.log(`  🔒 混淆: ${relativePath}`);

    const result = JavaScriptObfuscator.obfuscate(code, obfuscatorOptions);

    // 备份原文件（可选）
    // fs.writeFileSync(filePath + '.bak', code, 'utf8');

    // 写回混淆后的代码
    fs.writeFileSync(filePath, result.getObfuscatedCode(), 'utf8');

    stats.processed++;

  } catch (error) {
    console.error(`  ❌ 混淆失败: ${path.relative(outputDir, filePath)}`);
    console.error(`     错误: ${error.message}`);
    stats.failed++;
  }
}

/**
 * 打印统计信息
 */
function printStats() {
  console.log('\n📊 混淆统计:');
  console.log(`   ✅ 处理成功: ${stats.processed} 个文件`);
  console.log(`   ⏭️  跳过: ${stats.skipped} 个文件`);
  console.log(`   ❌ 失败: ${stats.failed} 个文件`);
}

// 主函数
function main() {
  console.log('═══════════════════════════════════════════');
  console.log('   🔒 GetJobs 前端代码混淆工具 v2.0');
  console.log('═══════════════════════════════════════════\n');

  console.log(`📁 输出目录: ${outputDir}`);

  if (!fs.existsSync(outputDir)) {
    console.error('❌ 目录不存在，请先运行 `npm run build`');
    process.exit(1);
  }

  console.log('\n⚙️  混淆配置:');
  console.log(`   • 控制流扁平化: 禁用 (避免破坏 chunk 加载)`);
  console.log(`   • 死代码注入: 禁用 (减少体积)`);
  console.log(`   • 字符串数组: 启用 (base64 编码)`);
  console.log(`   • 标识符前缀: _\n`);

  const startTime = Date.now();

  console.log('🚀 开始混淆...\n');
  obfuscateDirectory(outputDir);

  const endTime = Date.now();
  const duration = ((endTime - startTime) / 1000).toFixed(2);

  printStats();
  console.log(`\n⏱️  总耗时: ${duration}s`);
  console.log('\n✅ 混淆完成！\n');

  if (stats.failed > 0) {
    process.exit(1);
  }
}

main();
