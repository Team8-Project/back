package com.teamproj.backend.OAuth2.google.handler;


import com.teamproj.backend.OAuth2.google.OAuth2UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String JWT_SUBJECT = "user";
    private static final int JWT_DUE_DAY = 5;
    private static final String COOKIE_SUBJECT = "jwt";
    private static final int COOKIE_DUE_DAY = 5;

//    @Value("${spring.frontend.url}")
//    private String FRONTEND_URL;
//    @Value("${spring.frontend.domain}")
    private String COOKIE_DOMAIN;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2UserProvider OAuth2UserProvider = (OAuth2UserProvider) authentication.getPrincipal();

//        String jwt = JwtTokenProvider
//                .generateToken(OAuth2UserProvider.createJWTPayload(), JWT_SUBJECT, TimeUnit.DAYS.toMillis(JWT_DUE_DAY));
//
        
        // To Do : Header, Body에 넣어서 진행 할 수 있도록 변경
        // Header, Body 쿠키에 다 넣어서 주기
        Cookie cookie = new Cookie("cookiecheck", "체크중");
        cookie.setSecure(true);
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(COOKIE_DUE_DAY));
        cookie.setPath("/");
//        cookie.setDomain("COOKIE_DOMAIN");
        response.addCookie(cookie);
//
        response.sendRedirect("http://localhost:8080");
    }
}
