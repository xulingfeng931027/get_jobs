package com.getjobs.application.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 请求签名拦截器
 * 为所有外发 HTTP 请求添加时间戳、nonce 和签名
 * 防止抓包重放攻击
 */
@Slf4j
@Component
public class SignedRequestInterceptor implements ClientHttpRequestInterceptor {

    private final SecureRandom random = new SecureRandom();
    @Value("${secure.api.key:}")
    private String apiKey;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        // 跳过无 API Key 的请求（内部调用或测试）
        if (apiKey == null || apiKey.isEmpty()) {
            return execution.execute(request, body);
        }

        // 生成签名头
        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String signature = sign(request, timestamp, nonce, body);

        // 添加签名头
        request.getHeaders().add("X-API-Key", apiKey);
        request.getHeaders().add("X-Timestamp", String.valueOf(timestamp));
        request.getHeaders().add("X-Nonce", nonce);
        request.getHeaders().add("X-Signature", signature);

        // 添加请求 ID 便于追踪
        request.getHeaders().add("X-Request-ID", UUID.randomUUID().toString());

        log.debug("请求签名: method={}, uri={}, timestamp={}, nonce={}",
            request.getMethod(), request.getURI(), timestamp, nonce);

        return execution.execute(request, body);
    }

    /**
     * 生成随机数
     */
    private String generateNonce() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 生成签名
     * 签名内容: API_KEY + timestamp + nonce + method + uri + body
     */
    private String sign(HttpRequest request, long timestamp, String nonce, byte[] body) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(apiKey).append("|");
            sb.append(timestamp).append("|");
            sb.append(nonce).append("|");
            sb.append(request.getMethod()).append("|");
            sb.append(request.getURI()).append("|");
            if (body != null && body.length > 0) {
                sb.append(Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(body)));
            }

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            log.error("签名生成失败", e);
            return "";
        }
    }
}
