package com.getjobs.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源配置
 * 配置前端静态资源的访问路径
 */
@Slf4j
@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {
    private static final int FRONTEND_PORT = 6866;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 检查前端服务是否运行
        boolean hasFrontendService = detectFrontendService();

        // 检查静态资源路径（文件系统）
        Path distPath = Paths.get("src/main/resources/dist");
        Path staticPath = Paths.get("src/main/resources/static");

        boolean hasFileSystemDist = Files.exists(distPath);
        boolean hasFileSystemStatic = Files.exists(staticPath);

        // 检查 classpath 中的资源（适用于 JAR 包部署）
        boolean hasClasspathDist = hasClasspathResource("/dist/");
        boolean hasClasspathStatic = hasClasspathResource("/static/");

        boolean hasDistResources = hasFileSystemDist || hasClasspathDist;
        boolean hasStaticResources = hasFileSystemStatic || hasClasspathStatic;

        if (hasDistResources || hasStaticResources) {
            log.info("配置静态资源服务:");

            // 根据前端服务运行状态输出不同日志
            if (hasFrontendService) {
                log.info(" 使用前端开发服务 (端口 {})", FRONTEND_PORT);
            } else {
                if (hasFileSystemDist) {
                    log.info("使用文件系统 dist 目录: {}", distPath.toAbsolutePath());
                } else if (hasClasspathDist) {
                    log.info("使用 classpath dist 资源 (JAR包内)");
                }
                if (hasFileSystemStatic) {
                    log.info("使用文件系统 static 目录: {}", staticPath.toAbsolutePath());
                } else if (hasClasspathStatic) {
                    log.info("使用 classpath static 资源 (JAR包内)");
                }
            }

            // 配置静态资源处理器：确保文件系统路径优先、禁用缓存
            registry.addResourceHandler("/**")
                    .addResourceLocations(
                            "file:src/main/resources/dist/",
                            "file:src/main/resources/static/",
                            "classpath:/dist/",
                            "classpath:/static/"
                    )
                    .setCachePeriod(0)
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        @Override
                        protected Resource getResource(String resourcePath, Resource location) {
                            try {
                                Resource requestedResource = location.createRelative(resourcePath);

                                // 如果请求的资源存在，直接返回
                                if (requestedResource.exists() && requestedResource.isReadable()) {
                                    return requestedResource;
                                }

                                // Next.js 静态导出：无后缀路径优先匹配同名 .html / .txt
                                if (!resourcePath.startsWith("api/") && !resourcePath.contains(".")) {
                                    // 1) 尝试同名 .html
                                    Resource htmlResource = location.createRelative(resourcePath + ".html");
                                    if (htmlResource.exists() && htmlResource.isReadable()) {
                                        return htmlResource;
                                    }
                                    // 2) 尝试同名 .txt（App Router RSC 数据）
                                    Resource txtResource = location.createRelative(resourcePath + ".txt");
                                    if (txtResource.exists() && txtResource.isReadable()) {
                                        return txtResource;
                                    }
                                    // 3) 兜底返回 index.html（SPA fallback）
                                    Resource indexResource = location.createRelative("index.html");
                                    if (indexResource.exists() && indexResource.isReadable()) {
                                        return indexResource;
                                    }
                                }
                            } catch (IOException e) {
                                // 返回 null
                            }

                            return null;
                        }
                    });
        } else {
            log.warn("未找到静态资源目录 (dist 或 static)");
        }
    }

    /**
     * 检查 classpath 中是否存在资源
     */
    private boolean hasClasspathResource(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        return resource.exists();
    }

    /**
     * 检测前端开发服务是否在运行
     */
    private boolean detectFrontendService() {
        String[] hosts = {"127.0.0.1", "[::1]", "localhost"};

        for (String host : hosts) {
            try {
                String bareHost = host;
                if (bareHost.startsWith("[") && bareHost.endsWith("]")) {
                    bareHost = bareHost.substring(1, bareHost.length() - 1);
                }
                URI uri = new URI("http", null, bareHost, FRONTEND_PORT, "/", null, null);
                URL url = uri.toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.setInstanceFollowRedirects(false);

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                if (responseCode >= 200 && responseCode < 500) {
                    return true;
                }
            } catch (Exception e) {
                // 继续尝试下一个地址（包括IO/URI等异常）
            }
        }

        return false;
    }

    /**
     * 视图控制：将根路径转发到 index.html，确保 SPA 直接访问根路径正常
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
