package com.teamproj.backend.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;


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
                .select()
                .apis(RequestHandlerSelectors.any())
                // 스웨거가 RestController를 전부 스캔을 한다.
                // basePackage => 어디를 범위로 스캔을 할 것인지 작성
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())

                // 여기부터 jwt를 위한 설정
                .forCodeGeneration(true)
                .ignoredParameterTypes(java.sql.Date.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .securityContexts(Lists.newArrayList(securityContext()))
                .securitySchemes(Lists.newArrayList(authorizationKey()))
                .useDefaultResponseMessages(false);
    }

    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(API_NAME)
                .description(API_DESCRIPTION)
                .version(API_VERSION)
                .termsOfServiceUrl(API_TERMS_OF_SERVICE_URL)
                .build();
    }

    /**
     * authorizationKey
     *
     * @return
     */
    private ApiKey authorizationKey() {
        return new ApiKey("JWT_TOKEN", "Authorization", "header");
    }

    /**
     * securityContext
     *
     * @return
     */
    private springfox.documentation.spi.service.contexts.SecurityContext securityContext() {

        return springfox.documentation.spi.service.contexts.SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build();
    }

    /**
     * defaultAuth
     *
     * @return
     */
    private List<SecurityReference> defaultAuth() {

        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;

        return Lists.newArrayList(new SecurityReference("JWT_TOKEN", authorizationScopes),
                new SecurityReference("HTTP_REQUEST", authorizationScopes));
    }
    // 완료가 되었으면 오른쪽 URL 로 접속 => http://localhost:8080/swagger-ui.html
    // Swagger 3.0.0 이상 버전을 사용할 경우/swagger-ui 로 접속
}
