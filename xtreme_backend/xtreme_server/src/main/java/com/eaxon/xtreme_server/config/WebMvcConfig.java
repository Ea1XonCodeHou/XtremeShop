package com.eaxon.xtreme_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.eaxon.xtreme_server.interceptor.MerchantInterceptor;
import com.eaxon.xtreme_server.interceptor.UserInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserInterceptor userInterceptor;
    private final MerchantInterceptor merchantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端拦截器：保护用户资源接口 + 秒杀下单接口
        // /api/seckill/active 为公开接口（首页展示），不在拦截范围内
        registry.addInterceptor(userInterceptor)
                .addPathPatterns(
                        "/api/user/**",
                        "/api/seckill/order",
                        "/api/seckill/order/**",
                        "/api/seckill/orders"
                )
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/logout"
                );

        // 商家端拦截器：拦截 /api/merchant/** 业务接口（登录注册放行）
        registry.addInterceptor(merchantInterceptor)
                .addPathPatterns("/api/merchant/**")
                .excludePathPatterns(
                        "/api/merchant/login",
                        "/api/merchant/register",
                        "/api/merchant/logout"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Session-Id")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
