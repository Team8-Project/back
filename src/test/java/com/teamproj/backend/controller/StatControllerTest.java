package com.teamproj.backend.controller;

import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.KakaoUserService;
import com.teamproj.backend.service.StatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StatController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class StatControllerTest {
    private MockMvc mvc;

    @MockBean
    private StatService statService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("방문자 정보 수집")
    void statVisitor() throws Exception {
        mvc.perform(get("/api/stat/visitor"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("정답 결과 정산")
    void statQuizSolver() throws Exception {
        mvc.perform(get("/api/stat/quiz/1")
                        .param("score", "1"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("사전 Stat")
    void getStatDict() throws Exception {
        mvc.perform(get("/api/stat/dict")
                        .param("score", "1"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}