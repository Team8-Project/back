package com.teamproj.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000",
                                "http://localhost:8080",
                                "https://kauth.kakao.com/oauth/authorize/**",
                                "https://accounts.kakao.com/**",
                                "https://memegle.xyz",
                                "https://www.memegle.xyz",
                                "https://memeglememegle.xyz")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders(HttpHeaders.AUTHORIZATION);
    }
}