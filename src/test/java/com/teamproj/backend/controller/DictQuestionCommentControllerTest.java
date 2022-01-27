package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.comment.CommentPostRequestDto;
import com.teamproj.backend.dto.user.userInfo.UserNicknameModifyRequestDto;
import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.dict.DictQuestionCommentService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DictQuestionCommentController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class DictQuestionCommentControllerTest {

    private MockMvc mvc;

    @MockBean
    private DictQuestionCommentService dictQuestionCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }


    @Test
    @DisplayName("댓글 작성")
    void postComment() throws Exception {
        CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                .content("댓글 내용")
                .build();

        String content = objectMapper.writeValueAsString(commentPostRequestDto);

        mvc.perform(post("/api/dict/1/comment")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("댓글 삭제")
    void deleteComment() throws Exception {
        mvc.perform(delete("/api/dict/comment/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionCommentService, atLeastOnce()).deleteComment(null, 1L);
    }

    @Test
    @DisplayName("좋아요 댓글")
    void likeComment() throws Exception {
        mvc.perform(get("/api/dict/comment/like/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionCommentService, atLeastOnce()).likeComment(null, 1L);
    }
}