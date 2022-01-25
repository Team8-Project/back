package com.teamproj.backend.service.dict;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictCuriousTooRepository;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.config.S3MockConfig;
import com.teamproj.backend.dto.dict.question.DictQuestionResponseDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadRequestDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadResponseDto;
import com.teamproj.backend.dto.dict.question.detail.DictQuestionDetailResponseDto;
import com.teamproj.backend.dto.dict.question.search.DictQuestionSearchResponseDto;
import com.teamproj.backend.dto.dict.question.update.DictQuestionUpdateRequestDto;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.DictCuriousToo;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.model.dict.question.QuestionSelect;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Import(S3MockConfig.class)

@Transactional
@Rollback
@ExtendWith(MockitoExtension.class)
class DictQuestionServiceTest {

    @Autowired
    private DictQuestionService dictQuestionService;

    @Autowired
    private DictQuestionRepository dictQuestionRepository;

    @Autowired
    private DictCuriousTooRepository dictCuriousTooRepository;

    @Autowired
    private DictQuestionCommentRepository dictQuestionCommentRepository;

    @Autowired
    private QuestionSelectRepository questionSelectRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ServletRequestAttributes attributes;

    @Autowired
    S3Mock s3Mock;

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

    //region 질문 전체조회
    @Nested
    @DisplayName("게시글 전체조회")
    class getDictQuestion {

        @Test
        @DisplayName("성공 - 로그인 O")
        void getQuestion_success() {
            // given
            dictQuestionRepository.save(DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .thumbNail("thumbNail")
                    .user(user)
                    .build()
            );

            String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

            // when
            List<DictQuestionResponseDto> dictQuestionResponseDtoList = dictQuestionService.getQuestion(0, 1, token);

            // then
            for(DictQuestionResponseDto dictQuestionResponseDto : dictQuestionResponseDtoList) {
                assertNotNull(dictQuestionResponseDto);
            }
        }

        @Test
        @DisplayName("성공 - 로그인 X")
        void getQuestion_success2() {
            // given
            dictQuestionRepository.save(DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .thumbNail("thumbNail")
                    .user(user)
                    .build()
            );

            // when
            List<DictQuestionResponseDto> dictQuestionResponseDtoList = dictQuestionService.getQuestion(0, 1, "token");
        }
    }


    //endregion

    //region 질문 작성
    @Nested
    @DisplayName("질문 작성")
    class uploadQuestion {

        @Test
        @DisplayName("성공")
        void uploadQuestion_success() {
            // given
            DictQuestionUploadRequestDto dictQuestionUploadRequestDto = DictQuestionUploadRequestDto.builder()
                            .title(dictQuestionName)
                            .content(dictQuestionContent)
                            .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "testJunit", "originalName", null, "image".getBytes()
            );

            // when
            DictQuestionUploadResponseDto dictQuestionUploadResponseDto  = dictQuestionService.uploadQuestion(userDetails, dictQuestionUploadRequestDto, mockMultipartFile);

            // then
            assertEquals(dictQuestionUploadRequestDto.getTitle(), dictQuestionUploadResponseDto.getTitle());
            assertEquals(dictQuestionUploadRequestDto.getContent(), dictQuestionUploadResponseDto.getContent());
        }

        @Nested
        @DisplayName("실패")
        class uploadQuestion_fail {

            @Test
            @DisplayName("실패1 / 질문이름 Empty")
            void uploadQuestion_fail1() {
                // given
                DictQuestionUploadRequestDto dictQuestionUploadRequestDto = DictQuestionUploadRequestDto.builder()
                        .title("")
                        .content(dictQuestionContent)
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.uploadQuestion(userDetails, dictQuestionUploadRequestDto, mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.TITLE_IS_EMPTY, exception.getMessage());
            }

            @Test
            @DisplayName("실패2 / 질문내용 Empty")
            void uploadQuestion_fail2() {
                // given
                DictQuestionUploadRequestDto dictQuestionUploadRequestDto = DictQuestionUploadRequestDto.builder()
                        .title(dictQuestionName)
                        .content("")
                        .build();

                MockMultipartFile mockMultipartFile = new MockMultipartFile(
                        "testJunit", "originalName", null, "image".getBytes()
                );

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.uploadQuestion(userDetails, dictQuestionUploadRequestDto, mockMultipartFile);
                });

                // then
                assertEquals(ExceptionMessages.CONTENT_IS_EMPTY, exception.getMessage());
            }
        }
    }

    //endregion

    //region 질문 상세 조회
    @Nested
    @DisplayName("질문 상세 조회")
    class getQuestionDetail {

        @Nested
        @DisplayName("성공")
        class getQuestionDetail_success {
            @Test
            @DisplayName("성공1 / 좋아요 O")
            void getQuestionDetail_success1() {
                // given
                DictQuestion dictQuestion = DictQuestion.builder()
                                .questionName(dictQuestionName)
                                .content(dictQuestionContent)
                                .enabled(true)
                                .user(user)
                                .thumbNail("thumbNail")
                                .build();

                dictQuestionRepository.save(dictQuestion);

                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);


                // when
                DictQuestionDetailResponseDto dictQuestionDetailResponseDto = dictQuestionService.getQuestionDetail(
                        dictQuestion.getQuestionId(), token
                );

                // then
                assertEquals(dictQuestionDetailResponseDto.getQuestionId(), dictQuestion.getQuestionId());
                assertEquals(dictQuestionDetailResponseDto.getTitle(), dictQuestion.getQuestionName());
                assertEquals(dictQuestionDetailResponseDto.getContent(), dictQuestion.getContent());
                assertEquals(dictQuestionDetailResponseDto.getCommentCnt(), dictQuestion.getQuestionCommentList().size());
            }

            @Test
            @DisplayName("성공2 / 좋아요 X")
            void getQuestionDetail_success2() {
                // given
                DictQuestion dictQuestion = DictQuestion.builder()
                        .questionName(dictQuestionName)
                        .content(dictQuestionContent)
                        .enabled(true)
                        .user(user)
                        .thumbNail("thumbNail")
                        .build();

                dictQuestionRepository.save(dictQuestion);


                // when
                DictQuestionDetailResponseDto dictQuestionDetailResponseDto = dictQuestionService.getQuestionDetail(
                        dictQuestion.getQuestionId(), "token"
                );

                // then
                assertEquals(dictQuestionDetailResponseDto.getQuestionId(), dictQuestion.getQuestionId());
                assertEquals(dictQuestionDetailResponseDto.getTitle(), dictQuestion.getQuestionName());
                assertEquals(dictQuestionDetailResponseDto.getContent(), dictQuestion.getContent());
                assertEquals(dictQuestionDetailResponseDto.getCommentCnt(), dictQuestion.getQuestionCommentList().size());
            }

        }

        @Test
        @DisplayName("실패 / 유효하지 않거나 삭제된 질문입니다")
        void getQuestionDetail_fail() {
            // given
            List<DictQuestion> dictQuestionList = dictQuestionRepository.findAll();

            // when
            Exception exception = assertThrows(NullPointerException.class, () -> {
                dictQuestionService.getQuestionDetail(dictQuestionList.size() + 1L, "token");
            });

            // then
            assertEquals(ExceptionMessages.NOT_EXIST_QUESTION, exception.getMessage());
        }

    }
    //endregion

    //region 질문 업데이트(수정)
    @Nested
    @DisplayName("질문 업데이트(수정)")
    class updateQuestion {

        @Test
        @DisplayName("성공")
        void updateQuestion_success() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto = DictQuestionUpdateRequestDto.builder()
                    .title("변경된 질문")
                    .content("변경된 내용")
                    .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "testJunit", "originalName", null, "image".getBytes()
            );

            // when
            String result = dictQuestionService.updateQuestion(
                    dictQuestion.getQuestionId(), userDetails, dictQuestionUpdateRequestDto, mockMultipartFile

            );

            // then
            assertEquals("수정 완료", result);
        }

        @Test
        @DisplayName("실패 / 당신이 작성한 질문이 아닙니다")
        void updateQuestion_fail() {
            // given
            User user2 = User.builder()
                    .username("유저2아이디")
                    .nickname("유저2닉네임")
                    .password("유저2패스워드")
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

            DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto = DictQuestionUpdateRequestDto.builder()
                    .title("변경된 질문")
                    .content("변경된 내용")
                    .build();

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "testJunit", "originalName", null, "image".getBytes()
            );

            // when
            Exception exception =  assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.updateQuestion(
                    dictQuestion.getQuestionId(), userDetails, dictQuestionUpdateRequestDto, mockMultipartFile);
            });

            // then
            assertEquals(ExceptionMessages.NOT_MY_QUESTION, exception.getMessage());
        }

    }
    //endregion

    //region 질문 삭제
    @Nested
    @DisplayName("질문 삭제")
    class deleteQuestion {

        @Test
        @DisplayName("성공")
        void deleteQuestion_success() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            // when
            String result = dictQuestionService.deleteQuestion(userDetails, dictQuestion.getQuestionId());

            // then
            assertEquals("삭제 완료", result);
        }


        @Nested
        @DisplayName("실패")
        class deleteQuestion_fail {
            @Test
            @DisplayName("삭제할 질문이 없습니다")
            void deleteQuestion_fail1() {
                // given
                List<DictQuestion> dictQuestionList = dictQuestionRepository.findAll();

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    dictQuestionService.deleteQuestion(userDetails, dictQuestionList.size() + 1L);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_QUESTION, exception.getMessage());
            }

            @Test
            @DisplayName("삭제할 질문이 없음")
            void deleteQuestion_fail2() {
                // given
                List<DictQuestion> dictQuestionList = dictQuestionRepository.findAll();

                // when
                Exception exception = assertThrows(NullPointerException.class, () -> {
                    dictQuestionService.deleteQuestion(userDetails, dictQuestionList.size() + 1L);
                });

                // then
                assertEquals(ExceptionMessages.NOT_EXIST_QUESTION, exception.getMessage());
            }



            @Test
            @DisplayName("이미 채택한 질문은 삭제 X")
            void deleteQuestion_fail3() {
                // given
                DictQuestion dictQuestion = DictQuestion.builder()
                        .questionName(dictQuestionName)
                        .content(dictQuestionContent)
                        .enabled(true)
                        .user(user)
                        .thumbNail("thumbNail")
                        .build();
                dictQuestionRepository.save(dictQuestion);

                dictQuestion.setQuestionSelect(QuestionSelect.builder().build());


                // when

                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.deleteQuestion(userDetails, dictQuestion.getQuestionId());
                });

                // then
                assertEquals(ExceptionMessages.CAN_NOT_MODIFY_SELECT_QUESTION, exception.getMessage());
            }

        }
    }

    //endregion

    //region 질문 나도 궁금해요
    @Nested
    @DisplayName("질문 나도 궁금해요")
    class curiousTooQuestion {

        @Test
        @DisplayName("성공")
        void curiousTooQuestion_success1() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            // when
            boolean isLike = dictQuestionService.curiousTooQuestion(userDetails, dictQuestion.getQuestionId());


            // then
            assertEquals(true, isLike);
        }

        @Test
        @DisplayName("성공2")
        void curiousTooQuestion_success2() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();
            dictQuestionRepository.save(dictQuestion);

            dictCuriousTooRepository.save(DictCuriousToo.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .build());

            // when
            boolean isLike = dictQuestionService.curiousTooQuestion(userDetails, dictQuestion.getQuestionId());

            // then
            assertEquals(false, isLike);
        }
    }
    //endregion

    //region 질문 검색
    @Nested
    @DisplayName("질문 검색")
    class questionSearch {

        @Test
        @DisplayName("성공")
        void questionSearch_success() {
            // given
            String query = "제목";

            // when
            List<DictQuestionSearchResponseDto> dictQuestionSearchResponseDtoList = dictQuestionService.questionSearch(
                    user, query, 0, 5
            );


            // then
            assertNotEquals(dictQuestionSearchResponseDtoList.size(), 0);
        }

        @Nested
        @DisplayName("실패")
        class questionSearch_fail {

            @Test
            @DisplayName("검색어 2자 이하")
            void questionSearch_fail1() {
                // given
                String q = "";

                // when
                List<DictQuestionSearchResponseDto> dictQuestionSearchResponseDtoList = dictQuestionService.questionSearch(
                        user, q, 0, 5
                );

                // then
                assertEquals(new ArrayList<>(), dictQuestionSearchResponseDtoList);
            }
        }
    }
    //endregion

    //region 질문 채택
    @Nested
    @DisplayName("질문 채택")
    class selectAnswer {

        @Test
        @DisplayName("성공")
        void selectAnswer_success() {
            // given
            DictQuestion dictQuestion = DictQuestion.builder()
                    .questionName(dictQuestionName)
                    .content(dictQuestionContent)
                    .enabled(true)
                    .user(user)
                    .thumbNail("thumbNail")
                    .build();

            dictQuestionRepository.save(dictQuestion);

            User user2 = User.builder()
                    .username("유저2아이디")
                    .nickname("유저2")
                    .password("유저2비밀번호")
                    .profileImage("프로필 이미지")
                    .build();
            userRepository.save(user2);

            DictQuestionComment dictQuestionComment = DictQuestionComment.builder()
                    .dictQuestion(dictQuestion)
                    .user(user2)
                    .content("내용")
                    .build();
            dictQuestionCommentRepository.save(dictQuestionComment);

            // when
            String result = dictQuestionService.selectAnswer(userDetails, dictQuestionComment.getQuestionCommentId());


            // then
            assertEquals("채택 완료", result);
        }

        @Nested
        @DisplayName("실패")
        class selectAnswer_fail {

            @Test
            @DisplayName("이미 채택이 완료된 글입니다")
            void selectAnswer_fail1() {
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
                        .build();
                dictQuestionCommentRepository.save(dictQuestionComment);

                QuestionSelect questionSelect = QuestionSelect.builder()
                                .dictQuestion(dictQuestion)
                                .questionComment(dictQuestionComment)
                                .build();

                questionSelectRepository.save(questionSelect);

                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.selectAnswer(userDetails, dictQuestionComment.getQuestionCommentId());
                });

                // then
                assertEquals(ExceptionMessages.ALREADY_SELECT, exception.getMessage());
            }


            @Test
            @DisplayName("자기 자신을 채택할 수 없습니다")
            void selectAnswer_fail2() {
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
                        .build();
                dictQuestionCommentRepository.save(dictQuestionComment);


                // when
                Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                    dictQuestionService.selectAnswer(userDetails, dictQuestionComment.getQuestionCommentId());
                });

                // then
                assertEquals(ExceptionMessages.CAN_NOT_SELECT_MINE, exception.getMessage());
            }


        }

    }
    //endregion

    //region 카테고리별 게시글 총 개수
    @Test
    @DisplayName("카테고리별 게시글 총 개수")
    void getTotalQuestionCount() {
        // given
        DictQuestion dictQuestion = DictQuestion.builder()
                .questionName(dictQuestionName)
                .content(dictQuestionContent)
                .enabled(true)
                .user(user)
                .thumbNail("thumbNail")
                .build();
        dictQuestionRepository.save(dictQuestion);

        // when
        Long result = dictQuestionService.getTotalQuestionCount();

        // then
        assertNotEquals(0, result);
    }
    //endregion
}