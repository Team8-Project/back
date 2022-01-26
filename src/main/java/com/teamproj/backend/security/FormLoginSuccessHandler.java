package com.teamproj.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.user.login.LoginResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FormLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "BEARER";

    private final ObjectMapper mapper = new ObjectMapper();

    @Resource(name="jwtAuthenticateProcessor")
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException {
        final UserDetailsImpl userDetails = ((UserDetailsImpl) authentication.getPrincipal());
        // Token 생성
        final String token = JwtTokenUtils.generateJwtToken(userDetails);
        System.out.println(userDetails.getUsername() + "'s token : " + TOKEN_TYPE + " " + token);
        response.addHeader(AUTH_HEADER, TOKEN_TYPE + " " + token);


        //UserId, Nickname 내려주기
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        User user = jwtAuthenticateProcessor.getUser(userDetails);
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .userId(user.getId())
                .username(userDetails.getUsername())
                .nickname(user.getNickname())
                .build();
        ResponseDto<LoginResponseDto> responseDto = ResponseDto.<LoginResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("로그인 요청")
                .data(loginResponseDto)
                .build();

        String result = mapper.writeValueAsString(responseDto);
        response.getWriter().write(result);
        System.out.println("LOGIN SUCCESS!");
    }
}
