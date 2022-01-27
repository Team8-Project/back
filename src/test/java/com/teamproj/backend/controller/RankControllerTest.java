package com.teamproj.backend.controller;

import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.RankService;
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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RankController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class RankControllerTest {

    private MockMvc mvc;

    @MockBean
    private RankService rankService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("랭크 일주일")
    void getRankWeek() throws Exception {
        mvc.perform(get("/api/dict/rank/week"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(rankService, atLeastOnce()).getRank(7);
    }

    @Test
    @DisplayName("랭크 1달")
    void getRankMonth() throws Exception {
        mvc.perform(get("/api/dict/rank/month"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(rankService, atLeastOnce()).getRank(30);
    }

    @Test
    @DisplayName("사전")
    void getAllTimeDict() throws Exception {
        mvc.perform(get("/api/dict/rank/allTimeDict")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(rankService, atLeastOnce()).getAllTimeDictRank("token");
    }
}