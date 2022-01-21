package com.teamproj.backend.OAuth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.OAuth2.OAuth2UserProvider;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private static final String COOKIE_SUBJECT = "jwt";
    private static final int COOKIE_DUE_DAY = 5;

//    @Value("${spring.frontend.url}")
//    private String FRONTEND_URL;
//    @Value("${spring.frontend.domain}")

    private String COOKIE_DOMAIN = "http://localhost:8080/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 강제 로그인 처리
        final String AUTH_HEADER = "Authorization";
        final String TOKEN_TYPE = "BEARER";

        OAuth2UserProvider OAuth2UserProvider = (OAuth2UserProvider) authentication.getPrincipal();

        UserDetailsImpl userDetails = getUserDetail(OAuth2UserProvider); // 로그인처리 후 토큰 받아오기
        String jwt_token = JwtTokenUtils.generateJwtToken(userDetails);
        String tokenResult =  TOKEN_TYPE + " " + jwt_token;
        response.setHeader(AUTH_HEADER, tokenResult);

        Cookie cookie = new Cookie(COOKIE_SUBJECT, jwt_token);
        cookie.setSecure(true);
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(COOKIE_DUE_DAY));
        cookie.setPath("/");
//        cookie.setDomain(COOKIE_DOMAIN);
        response.addCookie(cookie);



        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(userDetails);
        response.getWriter().write(result);
        response.sendRedirect("http://localhost:8080/api/board/536");
    }

    private UserDetailsImpl getUserDetail(OAuth2UserProvider oAuth2UserProvider) {
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

        return userDetails;
    }

}
