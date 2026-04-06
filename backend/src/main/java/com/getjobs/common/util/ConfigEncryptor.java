package com.getjobs.common.util;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.stereotype.Component;

/**
 * 配置加密工具类
 * 用于加密和解密敏感配置信息（如数据库密码）
 * 
 * @author GetJobs
 * @since 2026-04-06
 */
@Component
public class ConfigEncryptor {

    /**
     * 加密密钥（生产环境应通过环境变量传入）
     */
    private static final String DEFAULT_ENCRYPTION_KEY = "get-jobs-encryption-key-2026";

    /**
     * 创建字符串加密器
     * 
     * @param encryptionKey 加密密钥
     * @return StringEncryptor 实例
     */
    public static StringEncryptor createEncryptor(String encryptionKey) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        config.setPassword(encryptionKey);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 加密字符串
     * 
     * @param plainText 明文
     * @param encryptionKey 加密密钥
     * @return 加密后的字符串
     */
    public static String encrypt(String plainText, String encryptionKey) {
        StringEncryptor encryptor = createEncryptor(encryptionKey);
        return encryptor.encrypt(plainText);
    }

    /**
     * 解密字符串
     * 
     * @param encryptedText 密文
     * @param encryptionKey 加密密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText, String encryptionKey) {
        StringEncryptor encryptor = createEncryptor(encryptionKey);
        return encryptor.decrypt(encryptedText);
    }

    /**
     * 生成 Jasypt 格式的加密字符串（ENC(...)）
     * 
     * @param plainText 明文
     * @param encryptionKey 加密密钥
     * @return Jasypt 格式的加密字符串
     */
    public static String encryptForYaml(String plainText, String encryptionKey) {
        String encrypted = encrypt(plainText, encryptionKey);
        return "ENC(" + encrypted + ")";
    }

    /**
     * 获取默认加密密钥
     * 生产环境应通过环境变量 JASYPT_ENCRYPTOR_PASSWORD 覆盖
     * 
     * @return 加密密钥
     */
    public static String getDefaultEncryptionKey() {
        String envKey = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        return envKey != null ? envKey : DEFAULT_ENCRYPTION_KEY;
    }
}
