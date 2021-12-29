package com.teamproj.backend.service;



import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.board.*;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.board.BoardHashTag;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback(value = true)

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ServletRequestAttributes attributes;


    UserDetailsImpl userDetails;

    String boardTitle;
    String boardContent;
    User user;


    @BeforeEach
    void setup() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        attributes = new ServletRequestAttributes(mockHttpServletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        boardTitle = "타이틀";
        boardContent = "내용";

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

    //region 게시글 전체조회
    @Nested
    @DisplayName("게시글 전체조회")
    class getBoard {
        
        @Test
        @DisplayName("성공")
        void getBoard_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("카테고리");
            boardCategoryRepository.save(boardCategory);

            // when
            List<BoardResponseDto> boardResponseDtoList = boardService.getBoard("카테고리");

            // then
            for(BoardResponseDto boardResponseDto : boardResponseDtoList) {
                assertNull(boardResponseDto);
            }
        }
        
        @Test
        @DisplayName("실패")
        void getBoard_fail() {

            // when
            Exception exception = assertThrows(NullPointerException.class, () -> {
                boardService.getBoard("없는 카테고리");
            });

            // then
            assertEquals("유효한 카테고리가 아닙니다.", exception.getMessage());
        }
    }
    //endregion

    //region 게시글 작성
    @Nested
    @DisplayName("게시글 작성")
    class uploadBoard {
        @Test
        @DisplayName("게시글 작성 / 성공")
        void uploadBoard_sucess() throws IOException {
            // givien
            BoardCategory boardCategory = new BoardCategory("카테고리");
            boardCategoryRepository.save(boardCategory);

            List<String> hashTags = new ArrayList<>();
            hashTags.add("tag1");
            hashTags.add("tag2");



            BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .hashTags(hashTags)
                    .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "image1", "image1", "application/doc", "image".getBytes()
            );

            // when
            BoardUploadResponseDto boardUploadResponseDto = boardService.uploadBoard(
                    userDetails, boardUploadRequestDto, boardCategory.getCategoryName(), mockMultipartFile
            );
            Optional<Board> board = boardRepository.findById(boardUploadResponseDto.getBoardId());

            System.out.println(board.get().isEnabled());
            // then
            assertEquals(board.get().getBoardId(), boardUploadResponseDto.getBoardId());
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
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "image1", "image1", "application/doc", "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.TITLE_IS_EMPTY, exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 내용 미입력")
            void uploadBoard_fail2() {
                // givien
                String boardContent = "";

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .build();


                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "image1", "image1", "application/doc", "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.CONTENT_IS_EMPTY, exception.getMessage());
            }

            @Test
            @DisplayName("실패3 / 해당 카테고리가 없습니다.")
            void uploadBoard_fail3() {
                // givien
                BoardCategory boardCategory = new BoardCategory("카테고리");
                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "image1", "image1", "application/doc", "image".getBytes()
                );

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리", mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_CATEGORY, exception.getMessage());
            }


            @Test
            @DisplayName("실패4 / 해쉬태그 5개까지 입력 가능")
            void uploadBoard_sucess() throws IOException {
                // givien
                BoardCategory boardCategory = new BoardCategory("카테고리");
                boardCategoryRepository.save(boardCategory);

                List<String> hashTags = new ArrayList<>();
                hashTags.add("tag1");
                hashTags.add("tag2");
                hashTags.add("tag3");
                hashTags.add("tag4");
                hashTags.add("tag5");
                hashTags.add("tag6");

                BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .hashTags(hashTags)
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "image1", "image1", "application/doc", "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.uploadBoard(userDetails, boardUploadRequestDto,
                            boardCategory.getCategoryName(), mockMultipartFile);
                });


                // then
                assertEquals(ExceptionMessages.HASHTAG_MAX_FIVE, exception.getMessage());
            }
        }
    }
    //endregion

    //region 게시글 상세 조회
    @Nested
    @DisplayName("게시글 상세 조회")
    class getBoardDetail {

        @Test
        @DisplayName("성공")
        void getBoardDetail_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("카테고리");
            boardCategoryRepository.save(boardCategory);


            Board board = Board.builder()
                    .user(user)
                    .content("내용")
                    .title("제목")
                    .boardCategory(boardCategory)
                    .thumbNail("썸네일URL")
                    .build();

            Comment comment = Comment.builder()
                    .board(board)
                    .content("내용")
                    .user(user)
                    .build();

            boardRepository.save(board);
            commentRepository.save(comment);


            String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

            // when
            BoardDetailResponseDto boardDetailResponseDto = boardService.getBoardDetail(board.getBoardId(), token);

            // then
            assertEquals(board.getBoardId(), boardDetailResponseDto.getBoardId());
            assertEquals(board.getTitle(), boardDetailResponseDto.getTitle());
            assertEquals(board.getContent(), boardDetailResponseDto.getContent());
            assertEquals(board.getUser().getNickname(), boardDetailResponseDto.getWriter());
        }

        @Test
        @DisplayName("실패 / 유효하지 않은 게시글입니다.")
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

    //region 게시글 업데이트(수정)
    @Nested
    @DisplayName("게시글 업데이트(수정)")
    class updateBoard {

        @Test
        @DisplayName("성공")
        void updateBoard_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("카테고리");
            Board board = Board.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .boardCategory(boardCategory)
                    .user(user)
                    .thumbNail("썸네일URL")
                    .build();

            boardCategoryRepository.save(boardCategory);
            userRepository.save(user);
            boardRepository.save(board);

            BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                    .title(board.getTitle())
                    .content(board.getContent())
                    .build();

            // when
            BoardUpdateResponseDto result = boardService.updateBoard(board.getBoardId(), userDetails, boardUpdateRequestDto);

            // then
            assertEquals("게시글 수정 완료", result.getResult());
        }


        @Nested
        @DisplayName("실패")
        class updateBoard_fail {

            @Test
            @DisplayName("실패 / 유효하지 않은 게시글입니다.")
            void updateBoard_fail() {
                // given
                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title("수정된 제목")
                        .content("수정된 내용")
                        .build();

                // when

                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.updateBoard(0L, userDetails, boardUpdateRequestDto);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 권한이 없습니다.")
            void updateBoard_fail2() {
                // given
                BoardCategory boardCategory = new BoardCategory("카테고리");
                User user2 = User.builder()
                        .username("유저네임2")
                        .nickname("닉네임2")
                        .password("qwer1234")
                        .build();

                boardCategoryRepository.save(boardCategory);
                userRepository.save(user2);

                Board board = Board.builder()
                        .title(boardTitle)
                        .content(boardContent)
                        .user(user2)
                        .boardCategory(boardCategory)
                        .thumbNail("썸네일URL")
                        .build();

                boardRepository.save(board);
                BoardUpdateRequestDto boardUpdateRequestDto = BoardUpdateRequestDto.builder()
                        .title(board.getTitle())
                        .content(board.getContent())
                        .build();



                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    boardService.updateBoard(board.getBoardId(), userDetails, boardUpdateRequestDto);
                });

                // then
                assertEquals(ExceptionMessages.NOT_MY_BOARD, exception.getMessage());
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
            // given

            BoardCategory boardCategory = new BoardCategory("카테고리");

            userRepository.save(user);
            boardCategoryRepository.save(boardCategory);

            Board board = Board.builder()
                    .user(user)
                    .boardCategory(boardCategory)
                    .content("콘텐츠")
                    .title("타이틀")
                    .thumbNail("썸네일URL")
                    .build();

            boardRepository.save(board);


            // when
            BoardDeleteResponseDto result = boardService.deleteBoard(userDetails, board.getBoardId());


            // then
            assertEquals("게시글 삭제 완료", result.getResult());
        }

        @Nested
        @DisplayName("게시글 삭제 / 실패")
        class deleteBoard_fail {

            @Test
            @DisplayName("실패1 / 유효하지 않은 게시글입니다.")
            void deleteBoard_fail() {

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.deleteBoard(userDetails, 0L);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_BOARD, exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 권한이 없습니다.")
            void deleteBoard_fail2() {
                // given

                BoardCategory boardCategory = new BoardCategory("카테고리");

                userRepository.save(user);
                boardCategoryRepository.save(boardCategory);

                Board board = Board.builder()
                        .user(user)
                        .boardCategory(boardCategory)
                        .content("콘텐츠")
                        .title("타이틀")
                        .thumbNail("썸네일URL")
                        .build();

                boardRepository.save(board);

                User user2 = User.builder()
                        .username("newuser2")
                        .nickname("닉네임22")
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

    //region 게시글 좋아요
    @Nested
    @DisplayName("게시글 좋아요")
    class boardLike {

        @Test
        @DisplayName("성공")
        void boardLike_success() {
            // given
            BoardCategory boardCategory = new BoardCategory("카테고리");

            Board board = Board.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .thumbNail("썸네일URL")
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
        @DisplayName("실패 / 유효하지 않은 게시글입니다.")
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

    //region 게시글 검색
    @Nested
    @DisplayName("게시글 검색")
    class boardSearch {

        @Test
        @DisplayName("게시글 검색 / 성공")
        void boardSearch_success() {
            // given
            List<BoardHashTag> boardHashTagList = new ArrayList<>();
            BoardHashTag boardHashTag = new BoardHashTag();
            boardHashTagList.add(boardHashTag);

            BoardCategory boardCategory = new BoardCategory("카테고리");
            Board board = Board.builder()
                    .title("타이틀")
                    .content("내용")
                    .user(user)
                    .boardCategory(boardCategory)
                    .thumbNail("썸네일URL")
                    .boardHashTagList(boardHashTagList)
                    .build();

            boardCategoryRepository.save(boardCategory);
            boardRepository.save(board);

            String keyword = "타이틀";

            // when
            List<BoardSearchResponseDto> boardSearchResponseDtoList = boardService.boardSearch(keyword);

            // then
            assertNotEquals(0, boardSearchResponseDtoList.size());
        }


        @Nested
        @DisplayName("게시글 검색 / 실패")
        class boardSearch_fail {


            @Test
            @DisplayName("실패 / 검색어.isEmpty()")
            void boardSearch_fail() {
                // given
                String searchWord = "";

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.boardSearch(searchWord);
                });

                // then
                assertEquals(ExceptionMessages.SEARCH_IS_EMPTY, exception.getMessage());
                System.out.println(exception.getMessage());
            }


            @Test
            @DisplayName("실패2 / 검색어 == null")
            void boardSearch_fail2() {
                // given
                String searchWord = null;

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.boardSearch(searchWord);
                });

                // then
                assertEquals(ExceptionMessages.SEARCH_IS_EMPTY, exception.getMessage());
            }


            @Test
            @DisplayName("실패3 / 검색하려는 게시글이 없습니다.")
            void boardSearch_fail3() {
                // given
                String keyword = "다른거";

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    boardService.boardSearch(keyword);
                });

                // then
                assertEquals(ExceptionMessages.SEARCH_BOARD_IS_EMPTY, exception.getMessage());
            }

        }

    }
    //endregion

    //region 추천 해시태그
    @Nested
    @DisplayName("추천 해시태그")
    class getRecommendHashTag {

        @Test
        @DisplayName("성공")
        void getRecommendHashTag_success() throws IOException {
            // given
            BoardCategory boardCategory = new BoardCategory("카테고리");
            boardCategoryRepository.save(boardCategory);

            List<String> hashTags = new ArrayList<>();
            hashTags.add("tag1");
            hashTags.add("tag2");
            hashTags.add("tag3");
            hashTags.add("tag4");
            hashTags.add("tag5");



            BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                    .title(boardTitle)
                    .content(boardContent)
                    .hashTags(hashTags)
                    .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "image1", "image1", "application/doc", "image".getBytes()
            );


            boardService.uploadBoard(userDetails, boardUploadRequestDto, boardCategory.getCategoryName(), mockMultipartFile);


            // when
            BoardHashTagResponseDto boardHashTagResponseDto = boardService.getRecommendHashTag();


            // then
            assertNotEquals(0, boardHashTagResponseDto.getHashTags().size());
        }
    }


    //endregion
}