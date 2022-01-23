package com.teamproj.backend.service.dict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.LoginRequestDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dict.mymeme.DictMyMemeResponseDto;
import com.teamproj.backend.dto.dict.search.DictSearchResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.dto.user.login.LoginResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class DictServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DictService dictService;
    @Autowired
    private DictRepository dictRepository;

    @Autowired
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;

    // BeforeEach Data
    Dict dict;
    Long dictId;
    String title;
    String content;
    String summary;
    String viewerIp;

    UserDetailsImpl userDetails;
    User user;
    String username;
    String nickname;
    String password;
    String token;

    @BeforeEach
    void setup() throws JsonProcessingException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 사용자 초기데이터 주입
        username = "test";
        nickname = "test";
        password = "a1234567";

//        signUp(username, nickname, password);
        token = logIn(username, password);

        userDetails = jwtAuthenticateProcessor.forceLogin(token);
        user = jwtAuthenticateProcessor.getUser(userDetails);

        // 사전 초기데이터 주입
        title = UUID.randomUUID().toString();
        content = UUID.randomUUID().toString();
        summary = UUID.randomUUID().toString().substring(0, 10);
        dict = dictRepository.save(Dict.builder()
                .firstAuthor(user)
                .recentModifier(user)
                .dictName(title)
                .summary(summary)
                .content(content)
                .build());
        dictId = dict.getDictId();

        viewerIp = "127.0.0.1";
    }

    String logIn(String username, String password) throws JsonProcessingException {
        LoginRequestDto dto = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        String apiUrl = "/api/user";
        HttpMethod method = HttpMethod.POST;

        String requestBody = mapper.writeValueAsString(dto);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<ResponseDto<LoginResponseDto>> response = restTemplate.exchange(
                apiUrl,
                method,
                request,
                new ParameterizedTypeReference<ResponseDto<LoginResponseDto>>() {
                });

        HttpHeaders responseHeader = response.getHeaders();
        return Objects.requireNonNull(responseHeader.get("Authorization")).get(0);
    }

    @Nested
    @DisplayName("사전 목록 조회")
    class getDict {
        @Test
        @DisplayName("성공")
        void getDict_success() {
            // given

            // when
            List<DictResponseDto> result = dictService.getDictList(0, 10, token);

            // then
            assertTrue(result.size() > 0);
        }
    }

//    @Nested
//    @DisplayName("사전 생성")
//    class postDict {
//        @Test
//        @DisplayName("성공")
//        void postDict_success() {
//            // given
//            String title = "test";
//
//            // when
//            postDict(title, summary, content);
//
//            // then
//            assertTrue(dictRepository.existsByDictName(title));
//        }
//
//        @Nested
//        @DisplayName("실패")
//        class postDict_fail {
//            @Test
//            @DisplayName("중복된 사전이름")
//            void postDict_fail_already_dictName() {
//                // given
//
//                // when
//                Exception exception = assertThrows(IllegalArgumentException.class,
//                        () -> postDict(title, summary, content)
//                );
//
//                // then
//                assertEquals(EXIST_DICT, exception.getMessage());
//            }
//        }
//    }

    @Nested
    @DisplayName("용어사전 수정")
    class putDict {
        @Test
        @DisplayName("성공")
        void dict_modify_success() {
            // given
            DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                    .content(UUID.randomUUID().toString().substring(0, 10))
                    .summary(UUID.randomUUID().toString().substring(0, 10))
                    .build();

            // when
            DictPutResponseDto dictPutResponseDto = dictService.putDict(userDetails, dictId, dictPutRequestDto);

            // then
            assertEquals("수정 성공", dictPutResponseDto.getResult());
        }


        @Nested
        @DisplayName("실패")
        class DictPut_fail {
            @Test
            @DisplayName("로그인하지 않은 사용자의 수정 시도")
            void dictPut_fail_non_login_user() {
                // given
                DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                        .content("변경")
                        .summary(UUID.randomUUID().toString())
                        .build();

                // when
                Long finalDictId = dictId;
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.putDict(null, finalDictId, dictPutRequestDto)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("존재하지 않는 글 수정 시도")
            void dictPut_fail_not_exist_dict() {
                // given
                DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                        .content(UUID.randomUUID().toString())
                        .summary(UUID.randomUUID().toString().substring(0, 10))
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.putDict(userDetails, 0L, dictPutRequestDto)
                );

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
            }

            @Test
            @DisplayName("30자 이상의 한줄요약으로 수정 시도")
            void dictPut_fail_too_long_summary() {
                // given
                DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                        .content(UUID.randomUUID().toString())
                        .summary(UUID.randomUUID().toString())
                        .build();

                // when
                Exception exception = assertThrows(IllegalArgumentException.class,
                        () -> dictService.putDict(userDetails, 0L, dictPutRequestDto)
                );

                // then
                assertEquals(SUMMARY_IS_TOO_BIG, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("용어사전 좋아요 / 좋아요 취소")
    class DictLike {
        @Nested
        @DisplayName("성공")
        class DictLike_success {
            @Test
            @DisplayName("좋아요")
            void dictLike_like_success() {
                // given

                // when
                DictLikeResponseDto dictLikeResponseDto = dictService.likeDict(userDetails, dictId);

                // then
                assertTrue(dictLikeResponseDto.isResult());
            }

            @Test
            @DisplayName("좋아요 취소")
            void dictLike_like_cancel_success() {
                // given
                // 좋아요
                dictService.likeDict(userDetails, dictId);

                // when
                // 좋아요 취소
                DictLikeResponseDto dictLikeResponseDto = dictService.likeDict(userDetails, dictId);

                // then
                assertFalse(dictLikeResponseDto.isResult());
            }
        }

        @Nested
        @DisplayName("실패")
        class DictLike_fail {
            @Test
            @DisplayName("로그인하지 않은 사용자의 좋아요 시도")
            void dictLike_fail_non_login_user() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.likeDict(null, dictId)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("존재하지 않는 글에 좋아요 시도")
            void dictLike_like_cancel_success() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.likeDict(userDetails, 0L)
                );

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("용어사전 상세")
    class getDictDetail {
        @Nested
        @DisplayName("성공")
        class getDictDetail_success {
            @Test
            @DisplayName("회원")
            void getDictDetail_success_login() {
                // given

                // when
                DictDetailResponseDto dictDetailResponseDto = dictService.getDictDetail(dictId, token, viewerIp);

                // then
                assertNotNull(dictDetailResponseDto);
            }

            @Test
            @DisplayName("비회원")
            void getDictDetail_success_non_login() {
                // given

                // when
                DictDetailResponseDto dictDetailResponseDto = dictService.getDictDetail(dictId, "", viewerIp);

                // then
                assertNotNull(dictDetailResponseDto);
            }
        }

        @Nested
        @DisplayName("실패")
        class DictDetail_fail {
            @Test
            @DisplayName("존재하지 않는 사전 열람 시도")
            void getDictDetail_fail_non_exist_dict() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.getDictDetail(0L, "", viewerIp));

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
            }
        }

        @Nested
        @DisplayName("기타")
        class DictDetail_etc {
            @Test
            @DisplayName("2회 연속 조회시 조회수 증가하지 않음 테스트")
            void getDictDetail_etc_views_2times() {
                // given

                // when
                dictService.getDictDetail(dictId, token, viewerIp);
                dictService.getDictDetail(dictId, token, viewerIp);

                // then
            }
        }
    }

    @Nested
    @DisplayName("용어사전 검색")
    class DictSearch {
        @Test
        @DisplayName("성공")
        void search_success() {
            // given
            String q = "오놀아놈";

            // when
            DictSearchResponseDto result = dictService.getSearchResult(token, q, 0, 10);

            // then
            assertTrue(result.getDictResult().size() > 0);
        }

        @Nested
        @DisplayName("실패")
        class DictSearch_fail {
            @Test
            @DisplayName("빈 문자열로 검색 시도")
            void search_fail_empty_word() {
                // given
                String q = "";

                // when
                DictSearchResponseDto result = dictService.getSearchResult(token, q, 0, 10);

                // then
                assertEquals(0, result.getDictResult().size());
            }
        }
    }

    @Nested
    @DisplayName("추천 검색어")
    class RecommendKeyword {
        @Test
        @DisplayName("성공")
        void getRecommendKeyword() {
            // given
            dictService.likeDict(userDetails, dictId);

            // when
            List<String> recommendList = dictService.getSearchInfo();

            // then
            assertTrue(recommendList.size() > 0);
        }
    }

    @Nested
    @DisplayName("스크랩한 사전 목록")
    class LikeDict {
        @Test
        @DisplayName("성공")
        void success() {
            // given

            // when
            List<DictMyMemeResponseDto> result = dictService.getMyMeme(userDetails);

            // then
            assertTrue(result.size() > 0);
        }
    }

    @Nested
    @DisplayName("메인페이지 오늘의 밈")
    class TodayMeme {
        @Test
        @DisplayName("성공")
        void success() {
            // given

            // when
            List<MainTodayMemeResponseDto> result = dictService.getTodayMeme(7);

            // then
            assertTrue(result.size() > 0);
        }
    }

    @Nested
    @DisplayName("사전페이지 오늘의 밈 카드")
    class TodayMemeCard {
        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("회원")
            void success_user() {
                // given

                // when
                List<DictBestResponseDto> result = dictService.getBestDict(token);

                // then
                assertTrue(result.size() > 0);
            }

            @Test
            @DisplayName("비회원")
            void success_non_user() {
                // given

                // when
                List<DictBestResponseDto> result = dictService.getBestDict("");

                // then
                assertTrue(result.size() > 0);
            }
        }
    }

    @Nested
    @DisplayName("사전 총 개수 요청")
    class DictCount {
        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("전체 개수 요청")
            void success_all_count() {
                // given

                // when
                Long count = dictService.getDictTotalCount(null);

                // then
                assertTrue(count > 0);
            }

            @Test
            @DisplayName("검색 결과 개수 요청")
            void success_query_result_count() {
                // given

                // when
                Long count = dictService.getDictTotalCount("오놀");

                // then
                assertTrue(count > 0);
            }
        }
    }

    @Nested
    @DisplayName("사전 이름 중복 확인")
    class DictNameCheck {
        @Nested
        @DisplayName("기존 중복 확인")
        class Original {
            @Test
            @DisplayName("중복되지 않는 사전 이름(사용가능)")
            void not_exist() {
                // given
                DictNameCheckRequestDto request = DictNameCheckRequestDto.builder()
                        .dictName(UUID.randomUUID().toString())
                        .build();

                // when
                DictNameCheckResponseDto result = dictService.checkDictName(request);

                // then
                assertTrue(result.isResult());
            }

            @Test
            @DisplayName("중복되는 사전 이름(사용불가)")
            void exist() {
                // given
                DictNameCheckRequestDto request = DictNameCheckRequestDto.builder()
                        .dictName("오놀아놈")
                        .build();

                // when
                DictNameCheckResponseDto result = dictService.checkDictName(request);

                // then
                assertFalse(result.isResult());
            }
        }

        @Nested
        @DisplayName("새 중복 확인")
        class New{
            @Test
            @DisplayName("중복되지 않는 사전 이름(사용가능)")
            void not_exist() {
                // given
                DictNameCheckRequestDto request = DictNameCheckRequestDto.builder()
                        .dictName(UUID.randomUUID().toString())
                        .build();

                // when
                DictNameCheckResponseDtoNeo result = dictService.neoCheckDictName(request);

                // then
                assertTrue(result.isResult());
            }

            @Test
            @DisplayName("중복되는 사전 이름(사용불가)")
            void exist() {
                // given
                DictNameCheckRequestDto request = DictNameCheckRequestDto.builder()
                        .dictName("오놀아놈")
                        .build();

                // when
                DictNameCheckResponseDtoNeo result = dictService.neoCheckDictName(request);

                // then
                assertFalse(result.isResult());
            }
        }
    }
}