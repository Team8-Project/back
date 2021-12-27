package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
class DictServiceTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DictService dictService;

    UserDetailsImpl userDetails;

    String dictName;
    String content;
    User user;

    @BeforeEach
    void setup() {
        dictName = "타이틀";
        content = "내용";

        user = User.builder()
                .username("유저네임")
                .nickname("닉네임")
                .password("Q1234567")
                .build();

        userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username("유저네임")
                .password("Q1234567")
                .build();
    }

    @Nested
    @DisplayName("사전 목록 조회")
    class getDict {
        @Test
        @DisplayName("성공")
        void getDict_success() {
            // given
            DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                    .title(UUID.randomUUID().toString())
                    .content(UUID.randomUUID().toString())
                    .build();
            dictService.postDict(userDetails, dictPostRequestDto);

            // when
            List<DictResponseDto> response = dictService.getDictList(0, 5, "");

            // then
            boolean result = false;
            for (DictResponseDto dictResponseDto : response) {
                if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                    result = true;
                    break;
                }
            }
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("사전 생성")
    class postDict {
        @Test
        @DisplayName("성공")
        void postDict_success() {
            // given
            DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                    .title(UUID.randomUUID().toString())
                    .content(UUID.randomUUID().toString())
                    .build();
            dictService.postDict(userDetails, dictPostRequestDto);

            // when
            List<DictResponseDto> response = dictService.getDictList(0, 5, "");

            // then
            boolean result = false;
            for (DictResponseDto dictResponseDto : response) {
                if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                    result = true;
                    break;
                }
            }
            assertTrue(result);
        }

        @Nested
        @DisplayName("실패")
        class postDict_fail {
            @Test
            @DisplayName("유효하지 않은 사용자")
            void postDict_fail_inValid_user() {
                // given
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.postDict(null, dictPostRequestDto)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("중복된 사전이름")
            void postDict_fail_already_dictName() {
                // given
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                // when
                Exception exception = assertThrows(IllegalArgumentException.class,
                        () -> dictService.postDict(userDetails, dictPostRequestDto)
                );

                // then
                assertEquals(EXIST_DICT, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("용어사전 수정")
    class putDict {
        @Test
        @DisplayName("성공")
        void dict_modify_success() {
            // given
            DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                    .title(UUID.randomUUID().toString())
                    .content(UUID.randomUUID().toString())
                    .build();
            dictService.postDict(userDetails, dictPostRequestDto);

            List<DictResponseDto> response = dictService.getDictList(0, 5, "");

            // id 찾기
            Long dictId = null;
            for (DictResponseDto dictResponseDto : response) {
                if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                    dictId = dictResponseDto.getDictId();
                    break;
                }
            }

            DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                    .content("변경")
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
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                        .content("변경")
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
                        .content("변경")
                        .build();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.putDict(userDetails, 0L, dictPutRequestDto)
                );

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
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
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                // when
                DictLikeResponseDto dictLikeResponseDto = dictService.likeDict(userDetails, dictId);

                // then
                assertTrue(dictLikeResponseDto.isResult());
            }

            @Test
            @DisplayName("좋아요 취소")
            void dictLike_like_cancel_success() {
                // given
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                // when
                // 좋아요
                dictService.likeDict(userDetails, dictId);
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
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                // when
                Long finalDictId = dictId;
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.likeDict(null, finalDictId)
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
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

                // when
                // 좋아요 처리
                dictService.likeDict(userDetails, dictId);
                DictDetailResponseDto dictDetailResponseDto = dictService.getDictDetail(dictId, token);


                // then
                assertEquals(dictPostRequestDto.getTitle(), dictDetailResponseDto.getTitle());
                assertEquals(dictPostRequestDto.getContent(), dictDetailResponseDto.getMeaning());
                assertTrue(dictDetailResponseDto.isLike());
            }

            @Test
            @DisplayName("비회원")
            void getDictDetail_success_non_login() {
                // given
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                List<DictResponseDto> response = dictService.getDictList(0, 5, "");

                // id 찾기
                Long dictId = null;
                for (DictResponseDto dictResponseDto : response) {
                    if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                        dictId = dictResponseDto.getDictId();
                        break;
                    }
                }

                // when
                DictDetailResponseDto dictDetailResponseDto = dictService.getDictDetail(dictId, "");
                // 좋아요 처리
                dictService.likeDict(userDetails, dictId);

                // then
                assertEquals(dictPostRequestDto.getTitle(), dictDetailResponseDto.getTitle());
                assertEquals(dictPostRequestDto.getContent(), dictDetailResponseDto.getMeaning());
                assertFalse(dictDetailResponseDto.isLike());
            }
        }

        @Nested
        @DisplayName("실패")
        class DictPut_fail {
            @Test
            @DisplayName("존재하지 않는 사전 열람 시도")
            void getDictDetail_fail_non_exist_dict() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictService.getDictDetail(0L, "")
                );

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("용어사전 검색")
    class DictSearch {
        @Nested
        @DisplayName("성공")
        class getDictDetail_success {
            @Test
            @DisplayName("제목 일치")
            void getDictDetail_success_title_match() {
                // given
                String title = UUID.randomUUID().toString();
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(title)
                        .content(UUID.randomUUID().toString())
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

                // when
                DictSearchResultResponseDto dictSearchResultResponseDtoList = dictService.getSearchResult(token, title, 0, 5).get(0);

                // then
                assertEquals(title, dictSearchResultResponseDtoList.getTitle());
            }

            @Test
            @DisplayName("내용 일치")
            void getDictDetail_success_content_match() {
                // given
                String content = UUID.randomUUID().toString();
                DictPostRequestDto dictPostRequestDto = DictPostRequestDto.builder()
                        .title(UUID.randomUUID().toString())
                        .content(content)
                        .build();
                dictService.postDict(userDetails, dictPostRequestDto);

                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

                // when
                DictSearchResultResponseDto dictSearchResultResponseDtoList = dictService.getSearchResult(token, content, 0, 5).get(0);

                // then
                assertEquals(content, dictSearchResultResponseDtoList.getMeaning());
            }
        }
    }

    @Nested
    @DisplayName("추천 검색어")
    class RecommendKeyword{
        @Test
        @DisplayName("성공")
        void getRecommendKeyword() {
            // given

            // when
            List<String> recommendList = dictService.getSearchInfo();

            // then
            assertTrue(recommendList.size()>0);
        }
    }
}