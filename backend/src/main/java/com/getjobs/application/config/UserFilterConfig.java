package com.getjobs.application.config;

import com.getjobs.application.filter.UserJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户 JWT 过滤器配置
 */
@Configuration
public class UserFilterConfig {

    @Autowired
    private UserJwtFilter userJwtFilter;

    @Bean
    public FilterRegistrationBean<UserJwtFilter> userJwtFilterRegistration() {
        FilterRegistrationBean<UserJwtFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(userJwtFilter);
        registration.addUrlPatterns("/api/user/*", "/api/subscription/*", "/api/billing/*");
        registration.setName("userJwtFilter");
        registration.setOrder(2);
        return registration;
    }
}
