package com.teamproj.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String API_NAME = "밈글밈글(가제)";
    private static final String API_DESCRIPTION = "밈글밈글 입니다.";
    private static final String API_VERSION = "0.8.0";
    private static final String API_TERMS_OF_SERVICE_URL = "https://github.com/Team8-Project";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .forCodeGeneration(true)
                .genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(java.sql.Date.class)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.any()) //package 설정
                .paths(PathSelectors.any()) //package 안에서 정해진 path 만 Swagger로 보여짐
                .build();
    }

    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(API_NAME)
                .description(API_DESCRIPTION)
                .version(API_VERSION)
                .termsOfServiceUrl(API_TERMS_OF_SERVICE_URL)
                .build();
    }
}
