package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardLikeRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.board.BoardSubjectRepository;
import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUploadResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.board.BoardSubject;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.UserDetailsServiceImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardCategoryRepository boardCategoryRepository;

    @Mock
    private BoardSubjectRepository boardSubjectRepository;

    @Mock
    private BoardLikeRepository boardLikeRepository;


    @Mock
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;

    @Mock
    private UserRepository userRepository;


    UserDetailsImpl userDetails;

    String boardTitle;
    String boardContent;
    User user;


    @BeforeEach
    void setup() {
        boardTitle = "타이틀";
        boardContent = "내용";

        user = User.builder()
                .username("유저네임")
                .nickname("닉네임")
                .password("Q1234567")
                .build();

        userDetails = UserDetailsImpl.builder()
                .username("유저네임")
                .password("q1w2E#")
                .build();
    }

    //region 게시글 전체조회
    @Nested
    @DisplayName("게시글 작성")
    class uploadBoard {
        @Test
        @DisplayName("게시글 작성 / 정상")
        void uploadBoard_sucess() {
            // givien
            BoardCategory boardCategory = new BoardCategory("카테고리", null);
            BoardSubject boardSubject = BoardSubject.builder()
                    .boardCategory(boardCategory)
                    .subject("서브젝트")
                    .build();

            when(boardCategoryRepository.findById(boardCategory.getCategoryName()))
                    .thenReturn(Optional.of(boardCategory));

            when(boardSubjectRepository.findBySubject(boardSubject.getSubject()))
                    .thenReturn(Optional.of(boardSubject));

            BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .subject(boardSubject.getSubject())
                    .category(boardCategory.getCategoryName())
                    .build();

            // when
            BoardUploadResponseDto boardUploadResponseDto = boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");

            // then
            assertNull(boardUploadResponseDto.getBoardId());
            assertEquals(boardTitle, boardUploadResponseDto.getTitle());
            assertEquals(boardContent, boardUploadResponseDto.getContent());
        }

        @Nested
        @DisplayName("게시글 작성 / 실패")
        class uploadBoard_fail {

            @Test
            @DisplayName("실패1 / 제목 미입력")
            void uploadBoard_fail() {
                // givien
                boardTitle = "";

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .subject(null)
                        .category(null)
                        .build();

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");
                        });


                // then
                assertEquals("제목은 필수 입력 값입니다", exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 내용 미입력")
            void uploadBoard_fail2() {
                // givien
                String boardContent = "";

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .subject(null)
                        .category(null)
                        .build();

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");
                });

                // then
                assertEquals("내용은 필수 입력 값입니다", exception.getMessage());
            }

            @Test
            @DisplayName("실패3 / 해당 카테고리가 없습니다.")
            void uploadBoard_fail3() {
                // givien
                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .subject(null)
                        .category(null)
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");
                });

                // then
                assertEquals("해당 카테고리가 없습니다.", exception.getMessage());
            }

            @Test
            @DisplayName("실패4 / 해당 글머리가 없습니다.")
            void uploadBoard_fail4() {
                // givien
                BoardCategory boardCategory = new BoardCategory("카테고리", null);

                when(boardCategoryRepository.findById(boardCategory.getCategoryName()))
                        .thenReturn(Optional.of(boardCategory));


                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .subject(null)
                        .category(boardCategory.getCategoryName())
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");
                });

                // then
                assertEquals("해당 글머리가 없습니다.", exception.getMessage());
            }
        }
    }
    //endregion
    
    //region 게시글 삭제
    @Nested
    @Transactional
    @DisplayName("게시글 삭제")
    class deleteBoard {

        @Test
        @DisplayName("게시글 삭제 / 성공")
        void deleteBoard_success() {

        }

        @Nested
        @DisplayName("게시글 삭제 / 실패")
        class deleteBoard_fail {

            @Test
            @DisplayName("실패1 / 해당 게시글이 없습니다.")
            void deleteBoard_fail() {

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.deleteBoard(userDetails, 1L);
                });

                // then
                assertEquals("해당 게시글이 없습니다.", exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 게시글을 작성한 유저만 삭제가 가능합니다.")
            void deleteBoard_fail2() {

                User user = User.builder()
                        .username("유저네임")
                        .nickname("닉네임")
                        .password("Q1234567")
                        .build();


                when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

                when(jwtAuthenticateProcessor.getUser(userDetails)).thenReturn(user);

                Board board = Board.builder()
                        .user(user)
                        .build();

                when(boardRepository.findById(board.getPostId())).thenReturn(Optional.of(board));


//                System.out.println(jwtAuthenticateProcessor.getUser(userDetails).getId());

                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.deleteBoard(userDetails, board.getPostId());
                });



                assertEquals("게시글을 작성한 유저만 삭제가 가능합니다.", exception.getMessage());
            }
        }
        
    }
    //endregion
}