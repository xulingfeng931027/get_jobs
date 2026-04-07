package com.getjobs.common.util;

/**
 * 配置加密工具 - 命令行工具
 * 用于生成加密后的配置字符串
 * 
 * 使用方法:
 * 1. 直接运行 main 方法
 * 2. 或使用 Maven 命令: mvn exec:java -Dexec.mainClass="com.getjobs.common.util.EncryptionTool"
 * 
 * @author GetJobs
 * @since 2026-04-06
 */
public class EncryptionTool {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  GetJobs 配置加密工具");
        System.out.println("========================================");
        System.out.println();

        // 获取加密密钥
        String encryptionKey = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            encryptionKey = System.getProperty("jasypt.encryptor.password");
        }
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            encryptionKey = ConfigEncryptor.getDefaultEncryptionKey();
            System.out.println("⚠️  未设置加密密钥，使用默认密钥: " + encryptionKey);
            System.out.println("💡 建议通过环境变量 JASYPT_ENCRYPTOR_PASSWORD 设置");
        } else {
            System.out.println("✅ 使用加密密钥: " + maskKey(encryptionKey));
        }
        System.out.println();

        // 如果有命令行参数，加密该参数
        if (args.length > 0) {
            String plainText = args[0];
            String encrypted = ConfigEncryptor.encryptForYaml(plainText, encryptionKey);
            System.out.println("📝 明文: " + plainText);
            System.out.println("🔒 密文: " + encrypted);
            System.out.println();
            System.out.println("📋 复制到 application.yml:");
            System.out.println("   password: " + encrypted);
        } else {
            // 交互式加密示例
            System.out.println("📌 示例加密:");
            System.out.println();
            
            String dbPassword = "7hxUKgrA7x6D!FZwHLFz";
            String encryptedPassword = ConfigEncryptor.encryptForYaml(dbPassword, encryptionKey);
            System.out.println("数据库密码:");
            System.out.println("  明文: " + dbPassword);
            System.out.println("  密文: " + encryptedPassword);
            System.out.println();
            
            String jwtSecret = "G3tJ0bs-Adm1n-Secr3t-K3y-2025!@#";
            String encryptedJwt = ConfigEncryptor.encryptForYaml(jwtSecret, encryptionKey);
            System.out.println("JWT Secret:");
            System.out.println("  明文: " + jwtSecret);
            System.out.println("  密文: " + encryptedJwt);
            System.out.println();
        }

        System.out.println("========================================");
        System.out.println("💡 使用提示:");
        System.out.println("1. 加密单个值:");
        System.out.println("   java -jar get_jobs.jar com.getjobs.common.util.EncryptionTool \"your_password\"");
        System.out.println();
        System.out.println("2. 设置自定义加密密钥:");
        System.out.println("   export JASYPT_ENCRYPTOR_PASSWORD=\"your-secret-key\"");
        System.out.println("   java -jar get_jobs.jar com.getjobs.common.util.EncryptionTool \"your_password\"");
        System.out.println();
        System.out.println("3. 启动应用时传入加密密钥:");
        System.out.println("   java -Djasypt.encryptor.password=\"your-secret-key\" -jar get_jobs.jar");
        System.out.println("========================================");
    }

    /**
     * 掩码显示加密密钥
     */
    private static String maskKey(String key) {
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
