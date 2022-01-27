package com.teamproj.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.GoogleUserService;
import com.teamproj.backend.service.KakaoUserService;
import com.teamproj.backend.service.NaverUserService;
import com.teamproj.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class UserControllerTest {

    private MockMvc mvc;

    @MockBean
    private KakaoUserService kakaoUserService;

    @MockBean
    private NaverUserService naverUserService;

    @MockBean
    private GoogleUserService googleUserService;

    @MockBean
    private UserService userService;


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String accessToken = "cLDCR4xs1TYyrJAIzQ9bzvXuzML37QiimPVuEwo9dVoAAAF7dvoLNw";


    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("회원가입 요청")
    public void signup() throws Exception {
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .username("유저네임")
                .nickname("닉네임이다")
                .password("Q1w2e3r4")
                .passwordCheck("Q1w2e3r4")
                .build();

        String content = objectMapper.writeValueAsString(signUpRequestDto);

        mvc.perform(post("/api/signup")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("카카오 소셜 로그인")
    public void kakaoLogin() throws Exception {
        mvc.perform(get("/api/user/kakao/callback")
                        .param("code", accessToken))
                .andExpect(status().isOk())
                .andDo(print());
        verify(kakaoUserService, atLeastOnce()).kakaoLogin(accessToken);
    }

    @Test
    @DisplayName("네이버 소셜 로그인")
    public void naverLogin() throws Exception {
        mvc.perform(get("/api/user/naver/callback")
                        .param("code", accessToken)
                        .param("state", "state"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(naverUserService, atLeastOnce()).naverLogin(accessToken, "state");
    }

    @Test
    @DisplayName("구글 소셜 로그인")
    public void googleLogin() throws Exception {
        mvc.perform(get("/api/user/google/callback")
                        .param("code", accessToken))
                .andExpect(status().isOk())
                .andDo(print());
        verify(googleUserService, atLeastOnce()).googleLogin(accessToken);
    }

//    @Test
//    @DisplayName("사용자 정보 요청 기능 수행")
//    public void userInfo() throws Exception {
//        UserDetailsImpl userDetails = UserDetailsImpl.builder()
//                        .username("유저네임")
//                        .password("패스워드")
//                        .build();
//
//        mvc.perform(post("/api/userInfo")
//                        .principal((Principal) userDetails))
//                .andExpect(status().isOk())
//                .andDo(print());
//    }

}
