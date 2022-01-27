package com.teamproj.backend.controller;

import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.MyPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MyPageController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class MyPageControllerTest {

    private MockMvc mvc;

    @MockBean
    private MyPageService myPageService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("마이페이지 정보 요청")
    void myPage() throws Exception {
        mvc.perform(get("/api/mypage")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(myPageService, atLeastOnce()).myPage(null);
    }

    @Test
    void profileImageModify() throws Exception {
        MockMultipartFile file = new MockMultipartFile("images", "images",
                "text/plain", "Some dataset...".getBytes());

        mvc.perform(multipart("/api/user/profileImage")
                        .file(file)
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(myPageService, atLeastOnce()).profileImageModify(null, file);
    }
}