package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.LoginRequestDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dict.mymeme.DictMyMemeResponseDto;
import com.teamproj.backend.dto.dict.search.DictSearchResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.user.login.LoginResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import com.teamproj.backend.service.dict.DictService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_DICT;
import static com.teamproj.backend.exception.ExceptionMessages.NOT_LOGIN_USER;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class DictControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mock;

    @Autowired
    private DictController dictController;
    @Autowired
    private DictService dictService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DictRepository dictRepository;

    // BeforeEach Data
    Dict dict;
    String viewerIp;

    UserDetailsImpl userDetails;
    User user;
    String token;

    @BeforeEach
    void setup() {
        mock = MockMvcBuilders.standaloneSetup(dictController).build();

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        user = userRepository.save(User.builder()
                .username("testtoast")
                .nickname("testtoast")
                .password("a1234567")
                .profileImage("")
                .alarmCheck(false)
                .build());

        userDetails = UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        token = JwtTokenUtils.generateJwtToken(userDetails);

        // 사전 초기데이터 주입
        dict = dictRepository.save(Dict.builder()
                .firstAuthor(user)
                .recentModifier(user)
                .dictName(UUID.randomUUID().toString())
                .summary(UUID.randomUUID().toString().substring(0, 10))
                .content(UUID.randomUUID().toString())
                .build());

        viewerIp = "127.0.0.1";
    }

    @Nested
    @DisplayName("사전 전체 목록 요청")
    class DictList {
        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("회원의 요청")
            void successUser() {
                // given

                // when
                ResponseDto<List<DictResponseDto>> result = dictController.getDictList(token, 0, 1);

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
            }

            @Test
            @DisplayName("비회원의 요청")
            void successNotUser() {
                // given

                // when
                ResponseDto<List<DictResponseDto>> result = dictController.getDictList(null, 0, 1);

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
            }
        }
    }

    @Nested
    @DisplayName("스크랩한 사전 목록 불러오기")
    class MyMeme {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            dictService.likeDict(userDetails, dict.getDictId());

            // when
            ResponseDto<List<DictMyMemeResponseDto>> result = dictController.getMyMeme(userDetails);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("실패 - 비회원이 요청")
        void failNotUser() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> dictController.getMyMeme(null));

            // then
            assertEquals(NOT_LOGIN_USER, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("사전이름 중복검사")
    class ExistCheck {
        @Test
        @DisplayName("사용 가능")
        void canUse() {
            // given
            DictNameCheckRequestDto dto = DictNameCheckRequestDto.builder()
                    .dictName(UUID.randomUUID().toString())
                    .build();

            // when
            ResponseDto<DictNameCheckResponseDto> result = dictController.checkDictName(dto);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().isResult());
        }

        @Test
        @DisplayName("사용 불가")
        void canNotUse() {
            // given
            DictNameCheckRequestDto dto = DictNameCheckRequestDto.builder()
                    .dictName(dict.getDictName())
                    .build();

            // when
            ResponseDto<DictNameCheckResponseDto> result = dictController.checkDictName(dto);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertFalse(result.getData().isResult());
        }
    }

    @Nested
    @DisplayName("사전 개수 요청")
    class DictCount {
        @Test
        @DisplayName("전체 개수 요청")
        void countAll() {
            // given

            // when
            ResponseDto<Long> result = dictController.getDictTotalCount(null);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData() > 0);
        }

        @Test
        @DisplayName("검색 결과 개수 요청")
        void countSearchResult() {
            // given

            // when
            ResponseDto<Long> result = dictController.getDictTotalCount(dict.getDictName());

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData() > 0);
        }
    }

    @Nested
    @DisplayName("사전 상세")
    class DictDetail {
        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("회원의 요청")
            void getDictDetailUser() {
                // given

                // when
                ResponseDto<DictDetailResponseDto> result = dictController.getDictDetail(token, dict.getDictId());

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
                assertEquals(dict.getDictName(), result.getData().getTitle());
            }

            @Test
            @DisplayName("비회원의 요청")
            void getDictDetailNotUser() {
                // given

                // when
                ResponseDto<DictDetailResponseDto> result = dictController.getDictDetail(null, dict.getDictId());

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
                assertEquals(dict.getDictName(), result.getData().getTitle());
            }
        }

        @Nested
        @DisplayName("실패")
        class Fail {
            @Test
            @DisplayName("유효하지 않은 사전")
            void notExistDict() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictController.getDictDetail(null, 0L));

                // then
                assertEquals(NOT_EXIST_DICT, exception.getMessage());
            }
        }
    }

    @Test
    @DisplayName("사전 작성")
    void postDict() {
        // given
        DictPostRequestDto dto = DictPostRequestDto.builder()
                .title(UUID.randomUUID().toString())
                .summary(UUID.randomUUID().toString().substring(0, 10))
                .content(UUID.randomUUID().toString())
                .build();

        // when
        ResponseDto<DictPostResponseDto> result = dictController.postDict(userDetails, dto);

        // then
        assertEquals(HttpStatus.OK.toString(), result.getStatus());
        assertEquals("작성 성공", result.getData().getResult());
    }

    @Test
    @DisplayName("사전 수정")
    void putDict() {
        // given
        DictPutRequestDto dto = DictPutRequestDto.builder()
                .summary("요약수정")
                .content("본문수정")
                .build();

        // when
        ResponseDto<DictPutResponseDto> result = dictController.putDict(userDetails, dict.getDictId(), dto);

        // then
        assertEquals(HttpStatus.OK.toString(), result.getStatus());
        assertEquals("수정 성공", result.getData().getResult());
    }

    @Nested
    @DisplayName("사전 좋아요")
    class LikeDict {
        @Test
        @DisplayName("좋아요")
        void successLike(){
            // given

            // when
            ResponseDto<DictLikeResponseDto> result = dictController.likeDict(userDetails, dict.getDictId());

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().isResult());
        }

        @Test
        @DisplayName("좋아요 취소")
        void successLikeCancel(){
            // given
            dictController.likeDict(userDetails, dict.getDictId());

            // when
            ResponseDto<DictLikeResponseDto> result = dictController.likeDict(userDetails, dict.getDictId());

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertFalse(result.getData().isResult());
        }
    }

    @Test
    @DisplayName("사전 역사 목록 불러오기")
    void dictHistory(){
        // given

        // when
        ResponseDto<DictHistoryResponseDto> result = dictController.getDictHistory(dict.getDictId());

        // then
        assertEquals(HttpStatus.OK.toString(), result.getStatus());
        assertEquals(dict.getDictId(), result.getData().getDictId());
    }

    @Nested
    @DisplayName("오늘의 밈 카드 요청")
    class BestMeme{
        @Test
        @DisplayName("회원의 요청")
        void getBestMemeUser(){
            // given
            dictController.likeDict(userDetails, dict.getDictId());

            // when
            ResponseDto<List<DictBestResponseDto>> result = dictController.getBestDict(token);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().size() > 0);
        }

        @Test
        @DisplayName("비회원의 요청")
        void getBestMemeNotUser(){
            // given
            dictController.likeDict(userDetails, dict.getDictId());

            // when
            ResponseDto<List<DictBestResponseDto>> result = dictController.getBestDict(null);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().size() > 0);
        }
    }

    @Nested
    @DisplayName("검색 결과 불러오기")
    class SearchResult{
        @Test
        @DisplayName("회원의 요청")
        void getSearchResultUser(){
            // given

            // when
            ResponseDto<DictSearchResponseDto> result = dictController.getSearchResult(token, dict.getDictName(), 0, 10);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().getDictResult().size() > 0);
        }

        @Test
        @DisplayName("비회원의 요청")
        void getSearchResultNotUser(){
            // given

            // when
            ResponseDto<DictSearchResponseDto> result = dictController.getSearchResult(null, dict.getDictName(), 0, 10);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().getDictResult().size() > 0);
        }
    }
}
