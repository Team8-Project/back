package com.teamproj.backend.service.dict;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.dto.comment.CommentDeleteResponseDto;
import com.teamproj.backend.dto.comment.CommentPostRequestDto;
import com.teamproj.backend.dto.comment.CommentPostResponseDto;
import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)

@Transactional
@Rollback
@ExtendWith(MockitoExtension.class)
class DictQuestionCommentServiceTest {

    @Autowired
    private DictQuestionCommentService dictQuestionCommentService;

    @Autowired
    private DictQuestionRepository dictQuestionRepository;

    @Autowired
    private DictQuestionCommentRepository dictQuestionCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ServletRequestAttributes attributes;

    UserDetailsImpl userDetails;

    String dictQuestionName;
    String dictQuestionContent;
    User user;

    @BeforeEach
    void setup() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        attributes = new ServletRequestAttributes(mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        dictQuestionName = "타이틀";
        dictQuestionContent = "내용";

        user = User.builder()
                .username("유저네임")
                .nickname("닉네임")
                .password("Q1234567")
                .build();

        userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username("유저네임")
                .password("q1w2E#")
                .build();
    }

    //region 댓글 목록 불러오기
    @Nested
    @DisplayName("댓글 목록 불러오기")
    class getCommentList {

        @Test
        @DisplayName("성공")
        void getCommentList_success() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();

            dictQuestionRepository.save(dictQuestion);

            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .content("내용")
                    .enabled(true)
                    .build();
            dictQuestionCommentRepository.save(dictQuestionComment);


            // when
            List<DictQuestionCommentResponseDto> commentResponseDtoList = dictQuestionCommentService.getCommentList(
                    dictQuestion.getQuestionId(), user, 1L
            );

            // then
            assertEquals(commentResponseDtoList.get(0).getCommentId(), dictQuestionComment.getQuestionCommentId());
            assertEquals(commentResponseDtoList.get(0).getCommentContent(), dictQuestionComment.getContent());
            assertEquals(commentResponseDtoList.get(0).getCommentWriter(), dictQuestionComment.getUser().getNickname());
        }
    }


    //endregion

    //region 댓글 작성
    @Nested
    @DisplayName("댓글 작성")
    class postComment {

        @Test
        @DisplayName("성공")
        void postComment_success() {
            // given
            User user2 = User.builder()
                    .nickname("test2nick")
                    .username("test2ID")
                    .password("testpwd")
                    .profileImage("testImg")
                    .build();
            userRepository.save(user2);

            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user2)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                    .content("내용")
                    .build();

            // when
            CommentPostResponseDto commentPostResponseDto = dictQuestionCommentService.postComment(
                    userDetails, dictQuestion.getQuestionId(), commentPostRequestDto
            );

            // then
            assertEquals(commentPostRequestDto.getContent(), commentPostResponseDto.getCommentContent());
        }
    }
    //endregion

    //region 댓글 삭제
    @Nested
    @DisplayName("댓글 삭제")
    class deleteComment {

        @Test
        @DisplayName("성공")
        void deleteComment_success() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .content("내용")
                    .enabled(true)
                    .build();
            dictQuestionCommentRepository.save(dictQuestionComment);


            // when
            CommentDeleteResponseDto commentDeleteResponseDto = dictQuestionCommentService.deleteComment(
                    userDetails, dictQuestionComment.getQuestionCommentId()
            );


            // then
            assertEquals("삭제 성공", commentDeleteResponseDto.getResult());
        }

    }

    //endregion

    //region 댓글 좋아요 / 취소
    @Nested
    @DisplayName("댓글 종아요 / 취소")
    class likeComment {

        @Test
        @DisplayName("댓글 좋아요")
        void likeComment_success1() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .content("내용")
                    .enabled(true)
                    .build();
            dictQuestionCommentRepository.save(dictQuestionComment);

            // when
            boolean result = dictQuestionCommentService.likeComment(userDetails, dictQuestionComment.getQuestionCommentId());

            // then
            assertEquals(true, result);
        }


        @Test
        @DisplayName("댓글 좋아요 취소")
        void likeComment_success2() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .content("내용")
                    .enabled(true)
                    .build();
            dictQuestionCommentRepository.save(dictQuestionComment);



            // when
            boolean result = dictQuestionCommentService.likeComment(userDetails, dictQuestionComment.getQuestionCommentId());

            // then
            assertEquals(true, result);
        }
    }

    //endregion
}