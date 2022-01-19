//package com.teamproj.backend.service.dict;
//
//import com.teamproj.backend.Repository.UserRepository;
//import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
//import com.teamproj.backend.Repository.dict.DictQuestionRepository;
//import com.teamproj.backend.config.S3MockConfig;
//import com.teamproj.backend.model.User;
//import com.teamproj.backend.model.dict.question.DictQuestion;
//import com.teamproj.backend.security.UserDetailsImpl;
//import io.findify.s3mock.S3Mock;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
//@Import(S3MockConfig.class)
//
//@Transactional
//@Rollback
//@ExtendWith(MockitoExtension.class)
//class DictQuestionCommentServiceTest {
//
//    @Autowired
//    private DictQuestionCommentService dictQuestionCommentService;
//
//    @Autowired
//    private DictQuestionRepository dictQuestionRepository;
//
//    @Autowired
//    private DictQuestionCommentRepository dictQuestionCommentRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Mock
//    private ServletRequestAttributes attributes;
//
//    @Autowired
//    S3Mock s3Mock;
//
//    UserDetailsImpl userDetails;
//
//    String dictQuestionName;
//    String dictQuestionContent;
//    User user;
//
//    @BeforeEach
//    void setup() {
//        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
//        attributes = new ServletRequestAttributes(mockHttpServletRequest);
//        RequestContextHolder.setRequestAttributes(attributes);
//
//        dictQuestionName = "타이틀";
//        dictQuestionContent = "내용";
//
//        user = User.builder()
//                .username("유저네임")
//                .nickname("닉네임")
//                .password("Q1234567")
//                .build();
//
//        userRepository.save(user);
//        userDetails = UserDetailsImpl.builder()
//                .username("유저네임")
//                .password("q1w2E#")
//                .build();
//    }
//
//    //region 댓글 목록 불러오기
//    @Nested
//    @DisplayName("댓글 목록 불러오기")
//    class getCommentList {
//
//        @Test
//        @DisplayName("성공")
//        void getCommentList_success() {
//            // given
//            DictQuestion dictQuestion = DictQuestion.builder()
//                    .questionName(dictQuestionName)
//                    .content(dictQuestionContent)
//                    .enabled(true)
//                    .user(user)
//                    .thumbNail("thumbNail")
//                    .build();
//
//            dictQuestionRepository.save(dictQuestion);
//
//            // when
//            dictQuestionCommentService.getCommentList(dictQuestion, user);
//
//            // then
//        }
//    }
//
//
//    //endregion
//
//}