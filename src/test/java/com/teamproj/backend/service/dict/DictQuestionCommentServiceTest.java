//package com.teamproj.backend.service.dict;
//
//import com.teamproj.backend.Repository.UserRepository;
//import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
//import com.teamproj.backend.Repository.dict.DictQuestionRepository;
//import com.teamproj.backend.Repository.dict.QuestionCommentLikeRepository;
//import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
//import com.teamproj.backend.dto.comment.CommentDeleteResponseDto;
//import com.teamproj.backend.dto.comment.CommentPostRequestDto;
//import com.teamproj.backend.dto.comment.CommentPostResponseDto;
//import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
//import com.teamproj.backend.exception.ExceptionMessages;
//import com.teamproj.backend.model.User;
//import com.teamproj.backend.model.dict.question.DictQuestion;
//import com.teamproj.backend.model.dict.question.DictQuestionComment;
//import com.teamproj.backend.model.dict.question.QuestionCommentLike;
//import com.teamproj.backend.model.dict.question.QuestionSelect;
//import com.teamproj.backend.security.UserDetailsImpl;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
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
//    private QuestionCommentLikeRepository questionCommentLikeRepository;
//
//    @Autowired
//    private QuestionSelectRepository questionSelectRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Mock
//    private ServletRequestAttributes attributes;
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
//        void getCommentList_success1() {
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
//            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                    .dictQuestion(dictQuestion)
//                    .user(user)
//                    .content("내용")
//                    .enabled(true)
//                    .build();
//            dictQuestionCommentRepository.save(dictQuestionComment);
//
//
//            // when
//            List<DictQuestionCommentResponseDto> commentResponseDtoList = dictQuestionCommentService.getCommentList(
//                    dictQuestion.getQuestionId(), user, 1L
//            );
//
//            // then
//            assertEquals(commentResponseDtoList.get(0).getCommentId(), dictQuestionComment.getQuestionCommentId());
//            assertEquals(commentResponseDtoList.get(0).getCommentContent(), dictQuestionComment.getContent());
//            assertEquals(commentResponseDtoList.get(0).getCommentWriter(), dictQuestionComment.getUser().getNickname());
//        }
//
//
//        @Test
//        @DisplayName("성공2")
//        void getCommentList_success2() {
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
//            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                    .dictQuestion(dictQuestion)
//                    .user(user)
//                    .content("내용")
//                    .enabled(true)
//                    .build();
//            dictQuestionCommentRepository.save(dictQuestionComment);
//
//            user = null;
//            // when
//            List<DictQuestionCommentResponseDto> commentResponseDtoList = dictQuestionCommentService.getCommentList(
//                    dictQuestion.getQuestionId(), user, 1L
//            );
//
//            // then
//            assertEquals(commentResponseDtoList.get(0).getCommentId(), dictQuestionComment.getQuestionCommentId());
//            assertEquals(commentResponseDtoList.get(0).getCommentContent(), dictQuestionComment.getContent());
//            assertEquals(commentResponseDtoList.get(0).getCommentWriter(), dictQuestionComment.getUser().getNickname());
//        }
//    }
//
//
//    //endregion
//
//    //region 댓글 작성
//    @Nested
//    @DisplayName("댓글 작성")
//    class postComment {
//
//        @Test
//        @DisplayName("성공")
//        void postComment_success() {
//            // given
//            User user2 = User.builder()
//                    .nickname("test2nick")
//                    .username("test2ID")
//                    .password("testpwd")
//                    .profileImage("testImg")
//                    .build();
//            userRepository.save(user2);
//
//            DictQuestion dictQuestion = DictQuestion.builder()
//                    .questionName(dictQuestionName)
//                    .content(dictQuestionContent)
//                    .enabled(true)
//                    .user(user2)
//                    .thumbNail("thumbNail")
//                    .build();
//            dictQuestionRepository.save(dictQuestion);
//
//            CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
//                    .content("내용")
//                    .build();
//
//            // when
//            CommentPostResponseDto commentPostResponseDto = dictQuestionCommentService.postComment(
//                    userDetails, dictQuestion.getQuestionId(), commentPostRequestDto
//            );
//
//            // then
//            assertEquals(commentPostRequestDto.getContent(), commentPostResponseDto.getCommentContent());
//        }
//    }
//    //endregion
//
//    //region 댓글 삭제
//    @Nested
//    @DisplayName("댓글 삭제")
//    class deleteComment {
//
//        @Test
//        @DisplayName("성공")
//        void deleteComment_success() {
//            // given
//            DictQuestion dictQuestion = DictQuestion.builder()
//                    .questionName(dictQuestionName)
//                    .content(dictQuestionContent)
//                    .enabled(true)
//                    .user(user)
//                    .thumbNail("thumbNail")
//                    .build();
//            dictQuestionRepository.save(dictQuestion);
//
//            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                    .dictQuestion(dictQuestion)
//                    .user(user)
//                    .content("내용")
//                    .enabled(true)
//                    .build();
//            dictQuestionCommentRepository.save(dictQuestionComment);
//
//
//            // when
//            CommentDeleteResponseDto commentDeleteResponseDto = dictQuestionCommentService.deleteComment(
//                    userDetails, dictQuestionComment.getQuestionCommentId()
//            );
//
//
//            // then
//            assertEquals("삭제 성공", commentDeleteResponseDto.getResult());
//        }
//
//
//        @Nested
//        @DisplayName("삭제")
//        class deleteComment_fail {
//
//            @Test
//            @DisplayName("자신의 댓글인 아닌경우")
//            void deleteComment_fail1() {
//                // given
//                DictQuestion dictQuestion = DictQuestion.builder()
//                        .questionName(dictQuestionName)
//                        .content(dictQuestionContent)
//                        .enabled(true)
//                        .user(user)
//                        .thumbNail("thumbNail")
//                        .build();
//                dictQuestionRepository.save(dictQuestion);
//
//                User user2 = User.builder()
//                        .username("유저2 아이디")
//                        .nickname("유저2 닉네임")
//                        .password("유저2")
//                        .profileImage("프로필 이미지")
//                        .build();
//                userRepository.save(user2);
//
//                DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                        .dictQuestion(dictQuestion)
//                        .user(user2)
//                        .content("내용")
//                        .enabled(true)
//                        .build();
//                dictQuestionCommentRepository.save(dictQuestionComment);
//
//
//                // when
//                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//                    dictQuestionCommentService.deleteComment(userDetails, dictQuestionComment.getQuestionCommentId());
//                });
//
//                // then
//                assertEquals(ExceptionMessages.NOT_MY_BOARD, exception.getMessage());
//            }
//
//            @Test
//            @DisplayName("채택된 댓글인 경우")
//            void deleteComment_fail2() {
//                // given
//                DictQuestion dictQuestion = DictQuestion.builder()
//                        .questionName(dictQuestionName)
//                        .content(dictQuestionContent)
//                        .enabled(true)
//                        .user(user)
//                        .thumbNail("thumbNail")
//                        .build();
//                dictQuestionRepository.save(dictQuestion);
//
//                DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                        .dictQuestion(dictQuestion)
//                        .user(user)
//                        .content("내용")
//                        .enabled(true)
//                        .build();
//                dictQuestionCommentRepository.save(dictQuestionComment);
//
//                QuestionSelect questionSelect = QuestionSelect.builder()
//                        .questionComment(dictQuestionComment)
//                        .dictQuestion(dictQuestion)
//                        .build();
//
//                questionSelectRepository.save(questionSelect);
//
//
//                // when
//                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//                    dictQuestionCommentService.deleteComment(userDetails, dictQuestionComment.getQuestionCommentId());
//                });
//
//                // then
//                assertEquals(ExceptionMessages.ALREADY_SELECT, exception.getMessage());
//            }
//
//        }
//
//    }
//
//    //endregion
//
//    //region 댓글 좋아요 / 취소
//    @Nested
//    @DisplayName("댓글 종아요 / 취소")
//    class likeComment {
//
//        @Test
//        @DisplayName("댓글 좋아요")
//        void likeComment_success1() {
//            // given
//            DictQuestion dictQuestion = DictQuestion.builder()
//                    .questionName(dictQuestionName)
//                    .content(dictQuestionContent)
//                    .enabled(true)
//                    .user(user)
//                    .thumbNail("thumbNail")
//                    .build();
//            dictQuestionRepository.save(dictQuestion);
//
//            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                    .dictQuestion(dictQuestion)
//                    .user(user)
//                    .content("내용")
//                    .enabled(true)
//                    .build();
//            dictQuestionCommentRepository.save(dictQuestionComment);
//
//            // when
//            boolean result = dictQuestionCommentService.likeComment(userDetails, dictQuestionComment.getQuestionCommentId());
//
//            // then
//            assertEquals(true, result);
//        }
//
//        @Test
//        @DisplayName("댓글 좋아요 취소")
//        void likeComment_success2() {
//            // given
//            DictQuestion dictQuestion = DictQuestion.builder()
//                    .questionName(dictQuestionName)
//                    .content(dictQuestionContent)
//                    .enabled(true)
//                    .user(user)
//                    .thumbNail("thumbNail")
//                    .build();
//            dictQuestionRepository.save(dictQuestion);
//
//            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
//                    .dictQuestion(dictQuestion)
//                    .user(user)
//                    .content("내용")
//                    .enabled(true)
//                    .build();
//            dictQuestionCommentRepository.save(dictQuestionComment);
//
//            QuestionCommentLike questionCommentLike = QuestionCommentLike.builder()
//                    .comment(dictQuestionComment)
//                    .user(user)
//                    .build();
//
//            questionCommentLikeRepository.save(questionCommentLike);
//
//            // when
//            boolean result = dictQuestionCommentService.likeComment(userDetails, dictQuestionComment.getQuestionCommentId());
//
//            // then
//            assertEquals(false, result);
//        }
//    }
//
//    //endregion
//}