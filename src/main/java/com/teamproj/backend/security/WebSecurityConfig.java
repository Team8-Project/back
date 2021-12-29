package com.teamproj.backend.security;

import com.teamproj.backend.security.filter.FormLoginFilter;
import com.teamproj.backend.security.filter.JwtAuthFilter;
import com.teamproj.backend.security.jwt.HeaderTokenExtractor;
import com.teamproj.backend.security.provider.FormLoginAuthProvider;
import com.teamproj.backend.security.provider.JWTAuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
@EnableGlobalMethodSecurity(securedEnabled = true) // @Secured 어노테이션 활성화
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //JWT 부분 시작
//    private final JwtTokenProvider jwtTokenProvider;
    private final JWTAuthProvider jwtAuthProvider;
    private final HeaderTokenExtractor headerTokenExtractor;

    public WebSecurityConfig(
            JWTAuthProvider jwtAuthProvider,
            HeaderTokenExtractor headerTokenExtractor
//            JwtTokenProvider jwtTokenProvider
    ) {
        this.jwtAuthProvider = jwtAuthProvider;
        this.headerTokenExtractor = headerTokenExtractor;
//        this.jwtTokenProvider = jwtTokenProvider;
    }
    //JWT부분 종료

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth
                .authenticationProvider(formLoginAuthProvider())
                .authenticationProvider(jwtAuthProvider);
    }

    @Override
    public void configure(WebSecurity web) {
    // h2-console 사용에 대한 허용 (CSRF, FrameOptions 무시)
        web
                .ignoring()
                .antMatchers("/h2-console/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers();

        http
                .cors()
                .and()
                .csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();

        /* 1.
         * UsernamePasswordAuthenticationFilter 이전에 FormLoginFilter, JwtFilter 를 등록합니다.
         * FormLoginFilter : 로그인 인증을 실시합니다.
         * JwtFilter       : 서버에 접근시 JWT 확인 후 인증을 실시합니다.
         */
        http
                .addFilterBefore(formLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                // 회원 관리 처리 API 전부를 login 없이 허용
                .antMatchers("/api/user").permitAll()
                // 그 외 어떤 요청이든 '인증'
                .anyRequest()
                .permitAll();
    }

    @Bean
    public FormLoginFilter formLoginFilter() throws Exception {
        FormLoginFilter formLoginFilter = new FormLoginFilter(authenticationManager());
        formLoginFilter.setFilterProcessesUrl("/api/user");
        formLoginFilter.setAuthenticationSuccessHandler(formLoginSuccessHandler());
        formLoginFilter.afterPropertiesSet();
        return formLoginFilter;
    }

    @Bean
    public FormLoginSuccessHandler formLoginSuccessHandler() {
        return new FormLoginSuccessHandler();
    }

    @Bean
    public FormLoginAuthProvider formLoginAuthProvider() {
        return new FormLoginAuthProvider(encodePassword());
    }

    private JwtAuthFilter jwtFilter() throws Exception {
        List<String> skipPathList = new ArrayList<>();

        skipPathList.add("GET,/h2-console");

        // 메인 페이지 API 허용
        skipPathList.add("GET,/api/main");

        // 회원 관리 API 허용
        skipPathList.add("POST,/api/user"); // 로그인
        skipPathList.add("GET,/api/user/**"); // 소셜로그인
        skipPathList.add("POST,/api/signup"); // 회원가입
        skipPathList.add("GET,/api/signup/**"); // 중복체크

        skipPathList.add("GET,/");

        // 사전 목록 API 허용
        skipPathList.add("GET,/api/dict*");
        skipPathList.add("GET,/api/dict/*");
        skipPathList.add("GET,/api/dict/*/history");
        skipPathList.add("GET,/api/dict/history/*");
        skipPathList.add("GET,/api/searchInfo/dict");
        skipPathList.add("GET,/api/count/dict");
        skipPathList.add("GET,/api/bestDict/dict");
        // 게시판 목록 API 허용
        skipPathList.add("GET,/api/board/subject");
        skipPathList.add("GET,/api/board/**");
        skipPathList.add("GET,/api/board/list/**");
        // 퀴즈 API 허용
        skipPathList.add("GET,/api/quiz/**");
        // 통계 API 허용
        skipPathList.add("GET,/stat");

        // Swagger 허용
        skipPathList.add("GET,/swagger-ui.html");
        skipPathList.add("GET,/swagger-resources/**");
        skipPathList.add("GET,/webjars/springfox-swagger-ui/**");
        skipPathList.add("GET,/v2/api-docs");


        FilterSkipMatcher matcher = new FilterSkipMatcher(
                skipPathList,
                "/**"
        );

        JwtAuthFilter filter = new JwtAuthFilter(
                matcher,
                headerTokenExtractor
        );
        filter.setAuthenticationManager(super.authenticationManagerBean());

        return filter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

