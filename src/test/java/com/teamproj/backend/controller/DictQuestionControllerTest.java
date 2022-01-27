package com.teamproj.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadRequestDto;
import com.teamproj.backend.dto.dict.question.update.DictQuestionUpdateRequestDto;
import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.dict.DictQuestionService;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DictQuestionController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)
@MockBean(JpaMetamodelMappingContext.class)
class DictQuestionControllerTest {

    private MockMvc mvc;

    @MockBean
    private DictQuestionService dictQuestionService;


    @Autowired
    private WebApplicationContext context;


    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("질문 게시판 목록 조회")
    public void getQuestion() throws Exception {
        mvc.perform(get("/api/dict/question")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).getQuestion(0, 5, "token");
    }

    @Test
    @DisplayName("질문 게시판 상세조회")
    public void getQuestionDetail() throws Exception {
        mvc.perform(get("/api/dict/question/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).getQuestionDetail(1L, "token");
    }

    @Test
    @DisplayName("질문 게시판 글 업로드")
    public void uploadQuestion() throws Exception {

        DictQuestionUploadRequestDto dictQuestionUploadRequestDto = DictQuestionUploadRequestDto.builder()
                .title("질문제목")
                .content("내용")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        MockMultipartFile file = new MockMultipartFile("thumbNail", "thumbNail",
                "text/plain", "Some dataset...".getBytes());

        MockMultipartFile file2 = new MockMultipartFile("dictQuestionUploadRequestDto", "dictQuestionUploadRequestDto",
                "application/json",  objectMapper.writeValueAsString(dictQuestionUploadRequestDto).getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/dict/question")
                        .file(file)
                        .file(file2)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("질문 게시판 글 업로드")
    public void putQuestion() throws Exception {

        DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto = DictQuestionUpdateRequestDto.builder()
                .title("질문제목")
                .content("내용")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        MockMultipartFile file = new MockMultipartFile("thumbNail", "dummy.csv",
                "text/plain", "Some dataset...".getBytes());
        // application/json if you pass json as string
        MockMultipartFile file2 = new MockMultipartFile("dictQuestionUpdateRequestDto", "dictQuestionUpdateRequestDto",
                "application/json",  objectMapper.writeValueAsString(dictQuestionUpdateRequestDto).getBytes(StandardCharsets.UTF_8));

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.fileUpload("/api/dict/question/1");
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });

        mvc.perform(builder
                        .file(file)
                        .file(file2)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("질문 게시글 삭제")
    public void deleteQuestion() throws Exception {
        mvc.perform(delete("/api/dict/question/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).deleteQuestion(null, 1L);
    }

    @Test
    @DisplayName("질문 나도 궁금해요")
    public void curiousTooQuestion() throws Exception {
        mvc.perform(get("/api/dict/question/curiousToo/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).curiousTooQuestion(null, 1L);
    }

    @Test
    @DisplayName("질문 답변(댓글) 선택")
    public void selectAnswer() throws Exception {
        mvc.perform(get("/api/dict/question/select/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).selectAnswer(null, 1L);
    }

    @Test
    @DisplayName("질문 갯수 조회")
    public void getQuestionCount() throws Exception {
        mvc.perform(get("/api/dict/question/count"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(dictQuestionService, atLeastOnce()).getTotalQuestionCount();
    }
}