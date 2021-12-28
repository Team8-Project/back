package com.teamproj.backend.kakaoLoginTest;

import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.controller.UserController;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.KakaoUserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class KakaoLoginTest {

    private MockMvc mvc;

    @MockBean
    private KakaoUserService kakaoUserService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;



    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }


    @Test
    @DisplayName("kakao소셜 로그인")
    public void kakaoLogin() throws Exception {
        mvc.perform(get("/api/user/kakao/callback")
                        .param("code", "code"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
        verify(kakaoUserService, atLeastOnce()).kakaoLogin("code");
    }
}