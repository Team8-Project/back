//package com.teamproj.backend.controller;
//
//import com.teamproj.backend.security.WebSecurityConfig;
//import com.teamproj.backend.service.board.BoardService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(
//        controllers = BoardController.class,
//        excludeFilters = {
//        @ComponentScan.Filter(
//                type = FilterType.ASSIGNABLE_TYPE,
//                classes = WebSecurityConfig.class
//        )
//})
//@ExtendWith(SpringExtension.class)
////@Transactional
////@Rollback
//@MockBean(JpaMetamodelMappingContext.class)
//class BoardControllerTest {
//
//    @Autowired
//    MockMvc mvc;
//
//    @MockBean
//    BoardService boardService;
//
//
//    @Test
//    @DisplayName("게시글 전체 조회")
//    void getBoard() throws Exception {
//        // given
//
//
//        // when
//
//        // then
//        mvc.perform(
//            get("/api/board/list/FREEBOARD"))
//            .andExpect(status().is(302));
//
//    }
//}