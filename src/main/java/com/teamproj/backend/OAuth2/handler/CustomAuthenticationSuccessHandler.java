package com.teamproj.backend.OAuth2.handler;


import com.teamproj.backend.OAuth2.OAuth2UserProvider;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.user.social.kakao.KakaoUserResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

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

        // 강제 로그인 처리
        final String AUTH_HEADER = "Authorization";
        final String TOKEN_TYPE = "BEARER";

        String jwt_token = forceLogin(OAuth2UserProvider); // 로그인처리 후 토큰 받아오기
        String tokenResult =  TOKEN_TYPE + " " + jwt_token;
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, tokenResult);

        KakaoUserResponseDto kakaoUserResponseDto = KakaoUserResponseDto.builder()
                .result("로그인 성공")
                .token(TOKEN_TYPE + " " + jwt_token)
                .build();
        System.out.println("Google user's token : " + TOKEN_TYPE + " " + jwt_token);
        System.out.println("LOGIN SUCCESS!");

        
        // To Do : Header, Body에 넣어서 진행 할 수 있도록 변경
        // Header, Body 쿠키에 다 넣어서 주기
        Cookie cookie = new Cookie(COOKIE_SUBJECT, jwt_token);
        cookie.setSecure(true);
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(COOKIE_DUE_DAY));
        cookie.setPath("/");
        cookie.setDomain(".naver.com");
        response.addCookie(cookie);

        response.sendRedirect("https://www.naver.com/");

//        getRedirectStrategy().sendRedirect(request, response, "/auth/success");
    }

    private String forceLogin(OAuth2UserProvider oAuth2UserProvider) {
        String email = oAuth2UserProvider.getEmail();
        User user = userRepository.findByUsername(email)
                .orElseThrow(
                        () -> new NullPointerException("없는 사용자입니다.")
                );

        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return JwtTokenUtils.generateJwtToken(userDetails);
    }

}
