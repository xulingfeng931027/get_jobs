package com.getjobs.application.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt 加密配置类
 * 自动配置字符串加密器，用于解密配置文件中的 ENC(...) 格式内容
 * 
 * @author GetJobs
 * @since 2026-04-06
 */
@Configuration
public class JasyptConfig {

    /**
     * 加密密钥，优先级：
     * 1. 环境变量 JASYPT_ENCRYPTOR_PASSWORD
     * 2. JVM 参数 -Djasypt.encryptor.password=xxx
     * 3. 配置文件中的默认值（仅开发环境使用）
     */
    @Value("${jasypt.encryptor.password:#{systemEnvironment['JASYPT_ENCRYPTOR_PASSWORD'] ?: 'get-jobs-encryption-key-2026'}}")
    private String encryptionKey;

    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
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
}
