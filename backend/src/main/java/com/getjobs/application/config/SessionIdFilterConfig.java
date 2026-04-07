package com.getjobs.application.config;

import com.getjobs.application.filter.SessionIdFilter;
import com.getjobs.application.filter.UserJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session ID 过滤器配置
 */
@Configuration
public class SessionIdFilterConfig {

    @Autowired
    private SessionIdFilter sessionIdFilter;

    @Bean
    public FilterRegistrationBean<SessionIdFilter> sessionIdFilterRegistration() {
        FilterRegistrationBean<SessionIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(sessionIdFilter);
        // 需要获取 userId 的路径
        registration.addUrlPatterns(
                "/api/zhilian/*",
                "/api/liepin/*",
                "/api/boss/*",
                "/api/51job/*",
                "/api/cookie/*"
        );
        registration.setName("sessionIdFilter");
        registration.setOrder(3); // 在 JWT Filter (Order=2) 之后执行
        return registration;
    }
}
