package com.teamproj.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadRequestDto;
import com.teamproj.backend.security.MockSpringSecurityFilter;
import com.teamproj.backend.security.WebSecurityConfig;
import com.teamproj.backend.service.BoardService;
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
        controllers = BoardController.class,
        excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfig.class
        )
})
@MockBean(JpaMetamodelMappingContext.class)
class BoardControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    BoardService boardService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }


    @Test
    @DisplayName("게시글 목록 불러오기")
    void getBoard() throws Exception {
        mvc.perform(get("/api/board/list/IMAGEBOARD")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).getBoard("IMAGEBOARD", 0, 5, "token");
    }

    @Test
    @DisplayName("게시글 작성")
    void uploadBoard() throws Exception {

        BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title("타이틀")
                        .content("내용")
                        .build();

        ObjectMapper objectMapper = new ObjectMapper();

        MockMultipartFile file = new MockMultipartFile("thumbNail", "dummy.csv",
                "text/plain", "Some dataset...".getBytes());
        // application/json if you pass json as string
        MockMultipartFile file2 = new MockMultipartFile("boardUploadRequestDto", "boardUploadRequestDto",
                "application/json",  objectMapper.writeValueAsString(boardUploadRequestDto).getBytes(StandardCharsets.UTF_8));


        mvc.perform(multipart("/api/board/IMAGEBOARD")
                        .file(file)
                        .file(file2)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세보기")
    void getBoardDetail() throws Exception {
        mvc.perform(get("/api/board/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).getBoardDetail(1L, "token");
    }

    @Test
    @DisplayName("게시글 수정")
    void updateBoard() throws Exception {
        BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                .title("타이틀")
                .content("내용")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        MockMultipartFile file = new MockMultipartFile("thumbNail", "dummy.csv",
                "text/plain", "Some dataset...".getBytes());

        MockMultipartFile file2 = new MockMultipartFile("boardUpdateRequestDto", "boardUpdateRequestDto",
                "application/json",  objectMapper.writeValueAsString(boardUpdateRequestDto).getBytes(StandardCharsets.UTF_8));


        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.fileUpload("/api/board/1");
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
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제")
    void deleteBoard() throws Exception {
        mvc.perform(delete("/api/board/1")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).deleteBoard(null, 1L);
    }

    @Test
    @DisplayName("게시글 좋아요")
    void boardLike() throws Exception {
        mvc.perform(get("/api/board/1/like")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).boardLike(null, 1L);
    }

    @Test
    @DisplayName("명예의 밈짤 받기 요청")
    void getBestMeme() throws Exception {
        mvc.perform(get("/api/board/IMAGEBOARD/best")
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).getBestMemeImg("IMAGEBOARD", "token");
    }

    @Test
    @DisplayName("게시글 총 개수 출력 요청")
    void getTotalBoardCount() throws Exception {
        mvc.perform(get("/api/board/count/IMAGEBOARD"))
                .andExpect(status().isOk())
                .andDo(print());
        verify(boardService, atLeastOnce()).getTotalBoardCount("IMAGEBOARD");
    }
}