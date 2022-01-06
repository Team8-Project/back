package com.teamproj.backend.OAuth2.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private static final String PARAM_NAME = "message";
    private static final String PATH = "/login";

//    @Value("${spring.frontend.url}")
//    private String FRONTEND_URL;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        System.out.println("실패");
//        String encodedMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
//
//        response.sendRedirect(
//                UriComponentsBuilder
//                        .fromUriString(FRONTEND_URL)
//                        .path(PATH)
//                        .queryParam(PARAM_NAME, encodedMessage)
//                        .build().toString()
//        );
    }
}