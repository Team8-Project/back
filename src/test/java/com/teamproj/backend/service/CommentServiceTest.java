package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.dto.comment.CommentPostRequestDto;
import com.teamproj.backend.dto.comment.CommentResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
class CommentServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardService boardService;
    @Autowired
    private CommentService commentService;

    UserDetailsImpl userDetails;
    UserDetailsImpl userDetails2;

    User user;
    User user2;

    Long postId;

    String category;

    @BeforeEach
    void setup() throws IOException {
        user = User.builder()
                .username("유저네임")
                .nickname("테스트닉네임")
                .password("Q1234567")
                .build();

        userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username("유저네임")
                .password("Q1234567")
                .build();

        user2 = User.builder()
                .username("유저네임2")
                .nickname("테스트닉네임2")
                .password("Q1234567")
                .build();
        userRepository.save(user2);
        userDetails2 = UserDetailsImpl.builder()
                .username("유저네임2")
                .password("Q1234567")
                .build();

        // 게시글 작성
        category = "FREEBOARD";
        String title = UUID.randomUUID().toString();
        BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                .title(title)
                .content(UUID.randomUUID().toString())
                .build();
        boardService.uploadBoard(userDetails, boardUploadRequestDto, category, null);

        // 게시글 조회
        List<BoardResponseDto> response = boardService.getBoard(category);
        for (BoardResponseDto boardResponseDto : response) {
            if (boardResponseDto.getTitle().equals(title)){
                postId = boardResponseDto.getBoardId();
            }
        }
    }

    @Nested
    @DisplayName("댓글 조회")
    class GetComment {
        @Test
        @DisplayName("성공")
        void getComment_success() {
            // given
            String test = "테스트댓글";
            CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                    .content(test)
                    .build();
            commentService.postComment(userDetails, postId, commentPostRequestDto);

            // when
            CommentResponseDto commentResponseDto = commentService.getCommentList(postId, 0, 1000).get(0);

            // then
            assertEquals(user.getUsername(), commentResponseDto.getCommentWriterId());
            assertEquals(user.getNickname(), commentResponseDto.getCommentWriter());
            assertEquals(test, commentResponseDto.getCommentContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글의 댓글 확인")
        void getComment_fail_not_exist_board() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> commentService.getCommentList(0L, 0, 1000)
            );

            // then
            assertEquals(NOT_EXIST_BOARD, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 작성")
    class PostComment {
        @Test
        @DisplayName("성공")
        void postComment_success() {
            // given
            String test = "테스트댓글";
            CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                    .content(test)
                    .build();

            // when
            commentService.postComment(userDetails, postId, commentPostRequestDto);

            // then
            CommentResponseDto commentResponseDto = commentService.getCommentList(postId, 0, 1000).get(0);
            assertEquals(user.getUsername(), commentResponseDto.getCommentWriterId());
            assertEquals(user.getNickname(), commentResponseDto.getCommentWriter());
            assertEquals(test, commentResponseDto.getCommentContent());
        }

        @Nested
        @DisplayName("실패")
        class PostComment_fail{
            @Test
            @DisplayName("존재하지 않는 게시글에 댓글 작성")
            void postComment_fail_comment_not_exist_board() {
                // given
                String test = "테스트댓글";
                CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                        .content(test)
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> commentService.postComment(userDetails, 0L, commentPostRequestDto)
                );

                // then
                assertEquals(NOT_EXIST_BOARD, exception.getMessage());
            }

            @Test
            @DisplayName("로그인하지 않은 사용자가 댓글 작성")
            void postComment_fail_non_login() {
                // given
                String test = "테스트댓글";
                CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                        .content(test)
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> commentService.postComment(null, postId, commentPostRequestDto)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @Test
        @DisplayName("성공")
        void deleteComment_success() {
            // given
            String test = "테스트댓글";
            CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                    .content(test)
                    .build();
            commentService.postComment(userDetails, postId, commentPostRequestDto);
            CommentResponseDto commentResponseDto = commentService.getCommentList(postId, 0, 1000).get(0);
            Long commentId = commentResponseDto.getCommentId();

            // when
            commentService.deleteComment(userDetails, commentId);

            // then
            assertEquals(0, boardService.getBoardDetail(postId, "").getCommentList().size());
        }

        @Nested
        @DisplayName("실패")
        class PostComment_fail{
            @Test
            @DisplayName("존재하지 않는 댓글 삭제 요청")
            void postComment_fail_comment_not_exist_board() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> commentService.deleteComment(userDetails, 0L)
                );

                // then
                assertEquals(NOT_EXIST_COMMENT, exception.getMessage());
            }

            @Test
            @DisplayName("로그인하지 않은 사용자가 댓글 삭제 요청")
            void postComment_fail_non_login() {
                // given
                String test = "테스트댓글";
                CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                        .content(test)
                        .build();
                commentService.postComment(userDetails, postId, commentPostRequestDto);
                CommentResponseDto commentResponseDto = commentService.getCommentList(postId, 0, 1000).get(0);
                Long commentId = commentResponseDto.getCommentId();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> commentService.deleteComment(null, commentId)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("자신의 것이 아닌 댓글 삭제 요청")
            void postComment_fail_not_mine() {
                // given
                String test = "테스트댓글";
                CommentPostRequestDto commentPostRequestDto = CommentPostRequestDto.builder()
                        .content(test)
                        .build();
                commentService.postComment(userDetails, postId, commentPostRequestDto);
                CommentResponseDto commentResponseDto = commentService.getCommentList(postId, 0, 1000).get(0);
                Long commentId = commentResponseDto.getCommentId();

                // when
                Exception exception = assertThrows(IllegalArgumentException.class,
                        () -> commentService.deleteComment(userDetails2, commentId)
                );

                // then
                assertEquals(NOT_MY_COMMENT, exception.getMessage());
            }
        }
    }
}