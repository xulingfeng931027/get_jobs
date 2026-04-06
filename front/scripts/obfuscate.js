/**
 * 前端代码混淆脚本
 * 在 Next.js 构建后对输出文件进行混淆处理
 */

const JavaScriptObfuscator = require('javascript-obfuscator');
const fs = require('fs');
const path = require('path');

// 混淆配置
const obfuscatorOptions = {
  // 代码压缩
  compact: true,
  
  // 控制流扁平化（降低可读性）
  controlFlowFlattening: true,
  controlFlowFlatteningThreshold: 0.75,
  
  // 死代码注入
  deadCodeInjection: true,
  deadCodeInjectionThreshold: 0.4,
  
  // 调试保护
  debugProtection: false, // 生产环境可设为 true
  debugProtectionInterval: 0,
  
  // 禁用 console
  disableConsoleOutput: false,
  
  // 标识符混淆方式
  identifierNamesGenerator: 'hexadecimal',
  
  // 保留标识符（不要混淆这些）
  reservedNames: [
    'next',
    'NEXT_DATA',
    '__NEXT_DATA__',
    'props',
    'page',
    'query',
    'buildId',
    'assetPrefix',
    'runtimeConfig',
    'nextExport',
    'isFallback',
    'gip',
    'appGip',
  ],
  
  // 字符串混淆
  stringArray: true,
  stringArrayEncoding: ['base64'],
  stringArrayThreshold: 0.75,
  
  // 转换对象键
  transformObjectKeys: true,
  
  // 变量名混淆
  renameGlobals: false,
  
  // 来源映射
  sourceMap: false,
  sourceMapMode: 'separate',
};

/**
 * 递归处理目录中的所有 JS 文件
 */
function obfuscateDirectory(dirPath) {
  const files = fs.readdirSync(dirPath);
  
  files.forEach(file => {
    const filePath = path.join(dirPath, file);
    const stat = fs.statSync(filePath);
    
    if (stat.isDirectory()) {
      // 跳过某些目录
      if (file === 'node_modules' || file === '.next') {
        return;
      }
      obfuscateDirectory(filePath);
    } else if (file.endsWith('.js') && !file.includes('.obfuscated.')) {
      // 混淆 JS 文件
      obfuscateFile(filePath);
    }
  });
}

/**
 * 混淆单个 JS 文件
 */
function obfuscateFile(filePath) {
  try {
    const code = fs.readFileSync(filePath, 'utf8');
    
    // 跳过已经混淆的文件
    if (code.includes('_0x') && code.includes('function _0x')) {
      console.log(`  ⏭️  跳过已混淆文件: ${filePath}`);
      return;
    }
    
    // 跳过太小的文件（可能是配置文件）
    if (code.length < 100) {
      console.log(`  ⏭️  跳过小文件: ${filePath}`);
      return;
    }
    
    console.log(`  🔒 混淆中: ${filePath}`);
    
    const result = JavaScriptObfuscator.obfuscate(code, obfuscatorOptions);
    
    // 写回文件
    fs.writeFileSync(filePath, result.getObfuscatedCode(), 'utf8');
    
  } catch (error) {
    console.error(`  ❌ 混淆失败 ${filePath}:`, error.message);
  }
}

// 主函数
function main() {
  // 获取输出目录（默认为 out 目录）
  const outputDir = process.argv[2] || path.join(__dirname, '..', 'out');
  
  console.log('🚀 开始混淆前端代码...');
  console.log(`📁 输出目录: ${outputDir}`);
  
  if (!fs.existsSync(outputDir)) {
    console.error(`❌ 目录不存在: ${outputDir}`);
    process.exit(1);
  }
  
  const startTime = Date.now();
  obfuscateDirectory(outputDir);
  const endTime = Date.now();
  
  console.log(`\n✅ 混淆完成！耗时: ${((endTime - startTime) / 1000).toFixed(2)}s`);
}

main();
