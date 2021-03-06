package com.teamproj.backend.service;


import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardLikeRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.board.BoardTodayLikeRepository;
import com.teamproj.backend.config.S3MockConfig;
import com.teamproj.backend.dto.board.BoardDelete.BoardDeleteResponseDto;
import com.teamproj.backend.dto.board.BoardDetail.BoardDetailResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardLikeResponseDto;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateResponseDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadResponseDto;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.board.BoardLike;
import com.teamproj.backend.model.board.BoardTodayLike;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Import(S3MockConfig.class)

@Transactional
@Rollback
@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private BoardLikeRepository boardLikeRepository;

    @Autowired
    private BoardTodayLikeRepository boardTodayLikeRepository;

    @Autowired
    private UserRepository userRepository;


    @Mock
    private ServletRequestAttributes attributes;

    @Autowired
    S3Mock s3Mock;

    UserDetailsImpl userDetails;

    String boardTitle;
    String boardContent;
    User user;


    @BeforeEach
    void setup() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        attributes = new ServletRequestAttributes(mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        boardTitle = "?????????";
        boardContent = "??????";

        user = User.builder()
                .username("???????????????")
                .nickname("??????????????????")
                .profileImage("??????????????????")
                .password("Q1234567")
                .build();

        userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username("???????????????")
                .password("Q1234567")
                .build();
    }

    //region ????????? ????????????
    @Nested
    @DisplayName("????????? ????????????")
    class getBoard {

        @Test
        @DisplayName("??????")
        void getBoard_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("IMAGEBOARD");
            boardCategoryRepository.save(boardCategory);

            // when
            List<BoardResponseDto> boardResponseDtoList = boardService.getBoard("IMAGEBOARD", 0, 1, "token");

            // then
            for(BoardResponseDto boardResponseDto : boardResponseDtoList) {
                assertNotNull(boardResponseDto);
            }
        }
    }
    //endregion

    //region ????????? ??????
    @Nested
    @DisplayName("????????? ??????")
    class uploadBoard {
        @Test
        @DisplayName("????????? ?????? / ??????")
        void uploadBoard_sucess() throws IOException {
            // givien
            BoardCategory boardCategory = new BoardCategory("????????????");
            boardCategoryRepository.save(boardCategory);

            BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "testJunit", "originalName", null, "image".getBytes()
            );

            // when
            BoardUploadResponseDto boardUploadResponseDto = boardService.uploadBoard(
                    userDetails, boardUploadRequestDto, boardCategory.getCategoryName(), mockMultipartFile
            );
            Optional<Board> board = boardRepository.findById(boardUploadResponseDto.getBoardId());

            // then
            assertEquals(board.get().getBoardId(), boardUploadResponseDto.getBoardId());
            assertEquals(boardTitle, boardUploadResponseDto.getTitle());
            assertEquals(boardContent, boardUploadResponseDto.getContent());
        }

        @Nested
        @DisplayName("????????? ?????? / ??????")
        class uploadBoard_fail {

            @Test
            @DisplayName("??????1 / ?????? ?????????")
            void uploadBoard_fail() {
                // givien
                boardTitle = "";

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "????????????", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.TITLE_IS_EMPTY, exception.getMessage());
            }

            @Test
            @DisplayName("??????2 / ?????? ?????????")
            void uploadBoard_fail2() {
                // givien
                String boardContent = "";

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .build();


                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "????????????", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.CONTENT_IS_EMPTY, exception.getMessage());
            }

            @Test
            @DisplayName("??????3 / ?????? ??????????????? ????????????.")
            void uploadBoard_fail3() {
                // givien
                BoardCategory boardCategory = new BoardCategory("????????????");
                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "????????????", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_CATEGORY, exception.getMessage());
            }
        }
    }
    //endregion

    //region ????????? ?????? ??????
    @Nested
    @DisplayName("????????? ?????? ??????")
    class getBoardDetail {

        @Test
        @DisplayName("??????")
        void getBoardDetail_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("????????????");
            boardCategoryRepository.save(boardCategory);


            Board board = Board.builder()
                    .user(user)
                    .content("??????")
                    .title("??????")
                    .boardCategory(boardCategory)
                    .enabled(true)
                    .thumbNail("?????????URL")
                    .build();

            boardRepository.save(board);


            String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

            // when
            BoardDetailResponseDto boardDetailResponseDto = boardService.getBoardDetail(board.getBoardId(), token);

            // then
            assertNull(boardDetailResponseDto.getTitle());
            assertNull(boardDetailResponseDto.getContent());
            assertNotNull(boardDetailResponseDto.getCreatedAt());
            assertNotNull(boardDetailResponseDto.getCommentCnt());
            assertEquals(board.getBoardId(), boardDetailResponseDto.getBoardId());
            assertEquals(board.getUser().getUsername(), boardDetailResponseDto.getUsername());
            assertEquals(board.getUser().getProfileImage(), boardDetailResponseDto.getProfileImageUrl());
            assertEquals(board.getViews(), boardDetailResponseDto.getViews());
            assertEquals(board.getBoardLikeList().size(), boardDetailResponseDto.getLikeCnt());
            assertEquals(false, boardDetailResponseDto.getIsLike());
            assertEquals(board.getThumbNail(), boardDetailResponseDto.getThumbNail());
            assertEquals(board.getUser().getNickname(), boardDetailResponseDto.getWriter());
        }

        @Test
        @DisplayName("?????? / ???????????? ?????? ??????????????????.")
        void getBoardDetail_fail() {
            // given
            String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);


            // when
            Exception exception = assertThrows(NullPointerException.class, () -> {
                boardService.getBoardDetail(0L, token);
            });

            // then
            assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
        }
    }
    //endregion

    //region ????????? ????????????(??????)
    @Nested
    @DisplayName("????????? ????????????(??????)")
    class updateBoard {


        @Nested
        @DisplayName("??????")
        class updateBoard_success {

            @Test
            @DisplayName("????????? ??????")
            void updateBoard_success1() {
                // given
                BoardCategory boardCategory = new BoardCategory("????????????");
                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .boardCategory(boardCategory)
                        .user(user)
                        .thumbNail("?????????URL")
                        .build();

                boardCategoryRepository.save(boardCategory);
                userRepository.save(user);
                boardRepository.save(board);

                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title(board.getTitle())
                        .content(board.getContent())
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                BoardUpdateResponseDto result = boardService.updateBoard(
                        board.getBoardId(), userDetails, boardUpdateRequestDto, mockMultipartFile
                );

                // then
                assertEquals("????????? ?????? ??????", result.getResult());
            }

            @Test
            @DisplayName("????????? Empty")
            void updateBoard_success2() {
                // given
                BoardCategory boardCategory = new BoardCategory("????????????");
                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .boardCategory(boardCategory)
                        .user(user)
                        .thumbNail("?????????URL")
                        .build();

                boardCategoryRepository.save(boardCategory);
                userRepository.save(user);
                boardRepository.save(board);

                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title(board.getTitle())
                        .content(board.getContent())
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "image", "", null, "".getBytes()
                );

                // when
                BoardUpdateResponseDto result = boardService.updateBoard(
                        board.getBoardId(), userDetails, boardUpdateRequestDto, mockMultipartFile
                );

                // then
                assertEquals("????????? ?????? ??????", result.getResult());
            }
        }


        @Nested
        @DisplayName("??????")
        class updateBoard_fail {

            @Test
            @DisplayName("?????? / ???????????? ?????? ??????????????????.")
            void updateBoard_fail() {
                // given
                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title("????????? ??????")
                        .content("????????? ??????")
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.updateBoard(0L, userDetails, boardUpdateRequestDto, mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
            }

            @Test
            @DisplayName("??????2 / ????????? ????????????.")
            void updateBoard_fail2() {
                // given
                BoardCategory boardCategory = new BoardCategory("????????????");
                User user2 = User.builder()
                        .username("????????????2")
                        .nickname("?????????2")
                        .password("qwer1234")
                        .build();

                boardCategoryRepository.save(boardCategory);
                userRepository.save(user2);

                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .user(user2)
                        .boardCategory(boardCategory)
                        .thumbNail("?????????URL")
                        .build();

                boardRepository.save(board);
                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title(board.getTitle())
                        .content(board.getContent())
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );


                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.updateBoard(
                            board.getBoardId(), userDetails, boardUpdateRequestDto, mockMultipartFile
                    );
                });

                // then
                assertEquals(ExceptionMessages.NOT_MY_BOARD, exception.getMessage());
            }
        }
    }

    //endregion

    //region ????????? ??????
    @Nested
    @Transactional
    @DisplayName("????????? ??????")
    class deleteBoard {

        @Test
        @DisplayName("????????? ?????? / ??????")
        void deleteBoard_success() {
            // given

            BoardCategory boardCategory = new BoardCategory("????????????");

            userRepository.save(user);
            boardCategoryRepository.save(boardCategory);

            Board board = Board.builder()
                    .user(user)
                    .boardCategory(boardCategory)
                    .content("?????????")
                    .title("?????????")
                    .thumbNail("?????????URL")
                    .build();

            boardRepository.save(board);


            // when
            BoardDeleteResponseDto result = boardService.deleteBoard(userDetails, board.getBoardId());


            // then
            assertEquals("????????? ?????? ??????", result.getResult());
        }

        @Nested
        @DisplayName("????????? ?????? / ??????")
        class deleteBoard_fail {

            @Test
            @DisplayName("??????1 / ???????????? ?????? ??????????????????.")
            void deleteBoard_fail() {

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.deleteBoard(userDetails, 0L);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
            }

            @Test
            @DisplayName("??????2 / ????????? ????????????.")
            void deleteBoard_fail2() {
                // given

                BoardCategory boardCategory = new BoardCategory("????????????");

                userRepository.save(user);
                boardCategoryRepository.save(boardCategory);

                Board board = Board.builder()
                        .user(user)
                        .boardCategory(boardCategory)
                        .content("?????????")
                        .title("?????????")
                        .thumbNail("?????????URL")
                        .build();

                boardRepository.save(board);

                User user2 = User.builder()
                        .username("newuser2")
                        .nickname("?????????22")
                        .password("Q1w2e3")
                        .build();

                userRepository.save(user2);
                UserDetailsImpl userDetails2 = UserDetailsImpl.builder()
                        .username(user2.getUsername())
                        .password(user2.getPassword())
                        .build();

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.deleteBoard(userDetails2, board.getBoardId());
                });

                // then
                assertEquals(ExceptionMessages.NOT_MY_BOARD, exception.getMessage());
            }
        }

    }
    //endregion

    //region ????????? ?????????
    @Nested
    @DisplayName("????????? ?????????")
    class boardLike {

        @Nested
        @DisplayName("??????")
        class boardLike_success {

            @Test
            @DisplayName("?????? ?????????1")
            void boardLike_success1() {
                // given
                BoardCategory boardCategory = new BoardCategory("????????????");

                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .thumbNail("?????????URL")
                        .boardCategory(boardCategory)
                        .user(user)
                        .build();


                boardCategoryRepository.save(boardCategory);
                userRepository.save(user);
                boardRepository.save(board);

                // when
                BoardLikeResponseDto result = boardService.boardLike(userDetails, board.getBoardId());

                // then
                assertEquals(true, result.getResult());
            }


            @Test
            @DisplayName("?????? ?????????2")
            void boardLike_success2() {
                // given
                BoardCategory boardCategory = new BoardCategory("IMAGEBOARD");

                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .thumbNail("?????????URL")
                        .boardCategory(boardCategory)
                        .user(user)
                        .build();

                BoardLike boardLike = BoardLike.builder()
                                .user(user)
                                .board(board)
                                .build();

                BoardTodayLike boardTodayLike = BoardTodayLike.builder()
                                .board(board)
                                .boardCategory(boardCategory)
                                .likeCount(1L)
                                .build();

                boardCategoryRepository.save(boardCategory);
                userRepository.save(user);
                boardRepository.save(board);
                boardLikeRepository.save(boardLike);
                boardTodayLikeRepository.save(boardTodayLike);

                // when
                BoardLikeResponseDto result = boardService.boardLike(userDetails, board.getBoardId());

                // then
                assertEquals(false, result.getResult());
            }
        }

        @Test
        @DisplayName("?????? / ???????????? ?????? ??????????????????.")
        void boardLike_fail() {

            // when
            Exception exception = assertThrows(NullPointerException.class, () -> {
                boardService.boardLike(userDetails, 0L);
            });

            // then
            assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
        }
    }
    //endregion

    //region ????????? ??????
    @Test
    @DisplayName("??????")
    void getTodayImage_success() {
        // when
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = boardService.getTodayImage(5);

        //then
        assertNotEquals(0, mainMemeImageResponseDtoList.size());
    }
    //endregion

    //region ??????????????? ????????? ??? ??????
    @Test
    @DisplayName("??????????????? ????????? ??? ?????? / ??????")
    void getTotalBoardCount_success() {
        // given
        BoardCategory boardCategory = BoardCategory.builder()
                .categoryName("IMAGEBOARD")
                .build();
        Long boardCount = boardRepository.countByBoardCategoryAndEnabled(boardCategory, true);

        // when
        Long result = boardService.getTotalBoardCount("IMAGEBOARD");

        // then
        assertEquals(boardCount, result);
    }

    //endregion
}