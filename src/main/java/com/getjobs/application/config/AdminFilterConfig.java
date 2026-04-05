package com.getjobs.application.config;

import com.getjobs.application.filter.AdminJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 后台管理员 JWT 过滤器配置
 */
@Configuration
public class AdminFilterConfig {

    @Autowired
    private AdminJwtFilter adminJwtFilter;

    @Bean
    public FilterRegistrationBean<AdminJwtFilter> adminJwtFilterRegistration() {
        FilterRegistrationBean<AdminJwtFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(adminJwtFilter);
        registration.addUrlPatterns("/api/admin/*");
        registration.setName("adminJwtFilter");
        registration.setOrder(1);
        return registration;
    }
}
