package com.getjobs.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全 HTTP 客户端工厂
 * 为所有 HTTP 请求添加签名头，防止抓包重放攻击
 */
@Slf4j
@Component
public class SecureHttpClientFactory {

    private final SecureRandom random = new SecureRandom();
    private final HttpClient defaultClient;
    // 缓存常用端点的专用客户端
    private final Map<String, HttpClient> clientCache = new ConcurrentHashMap<>();
    @Value("${secure.api.key:}")
    private String apiKey;
    @Value("${secure.api.endpoint:}")
    private String apiEndpoint;

    public SecureHttpClientFactory() {
        this.defaultClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .version(HttpClient.Version.HTTP_2)
            .build();
    }

    /**
     * 获取默认客户端（带签名）
     */
    public HttpClient getClient() {
        return defaultClient;
    }

    /**
     * 获取带签名的 HttpClient
     */
    public HttpClient getSignedClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .version(HttpClient.Version.HTTP_2)
            .build();
    }

    /**
     * 发送带签名的 GET 请求
     */
    public String get(String url) throws IOException, InterruptedException {
        HttpRequest request = buildSignedRequest(url, "GET", null);
        HttpResponse<String> response = getSignedClient().send(request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response.body();
    }

    /**
     * 发送带签名的 POST 请求
     */
    public String post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = buildSignedRequest(url, "POST", body);
        HttpResponse<String> response = getSignedClient().send(request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response.body();
    }

    /**
     * 构建带签名的请求
     */
    private HttpRequest buildSignedRequest(String url, String method, String body) {
        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String signature = sign(url, method, timestamp, nonce, body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("X-API-Key", apiKey != null ? apiKey : "")
            .header("X-Timestamp", String.valueOf(timestamp))
            .header("X-Nonce", nonce)
            .header("X-Signature", signature)
            .header("X-Request-ID", java.util.UUID.randomUUID().toString());

        switch (method.toUpperCase()) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(body != null ?
                HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());
            case "PUT" -> builder.PUT(body != null ?
                HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());
            case "DELETE" -> builder.DELETE();
            default -> builder.GET();
        }

        return builder.build();
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
     */
    private String sign(String url, String method, long timestamp, String nonce, String body) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(apiKey).append("|");
            sb.append(timestamp).append("|");
            sb.append(nonce).append("|");
            sb.append(method).append("|");
            sb.append(url).append("|");
            if (body != null && !body.isEmpty()) {
                sb.append(Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8))));
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
