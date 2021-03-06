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

        // ?????? ??????????????? ??????
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
    @DisplayName("?????? ?????? ?????? ??????")
    class DictList {
        @Nested
        @DisplayName("??????")
        class Success {
            @Test
            @DisplayName("????????? ??????")
            void successUser() {
                // given

                // when
                ResponseDto<List<DictResponseDto>> result = dictController.getDictList(token, 0, 1);

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
            }

            @Test
            @DisplayName("???????????? ??????")
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
    @DisplayName("???????????? ?????? ?????? ????????????")
    class MyMeme {
        @Test
        @DisplayName("??????")
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
        @DisplayName("?????? - ???????????? ??????")
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
    @DisplayName("???????????? ????????????")
    class ExistCheck {
        @Test
        @DisplayName("?????? ??????")
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
        @DisplayName("?????? ??????")
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
    @DisplayName("?????? ?????? ??????")
    class DictCount {
        @Test
        @DisplayName("?????? ?????? ??????")
        void countAll() {
            // given

            // when
            ResponseDto<Long> result = dictController.getDictTotalCount(null);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData() > 0);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????")
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
    @DisplayName("?????? ??????")
    class DictDetail {
        @Nested
        @DisplayName("??????")
        class Success {
            @Test
            @DisplayName("????????? ??????")
            void getDictDetailUser() {
                // given

                // when
                ResponseDto<DictDetailResponseDto> result = dictController.getDictDetail(token, dict.getDictId());

                // then
                assertEquals(HttpStatus.OK.toString(), result.getStatus());
                assertEquals(dict.getDictName(), result.getData().getTitle());
            }

            @Test
            @DisplayName("???????????? ??????")
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
        @DisplayName("??????")
        class Fail {
            @Test
            @DisplayName("???????????? ?????? ??????")
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
    @DisplayName("?????? ??????")
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
        assertEquals("?????? ??????", result.getData().getResult());
    }

    @Test
    @DisplayName("?????? ??????")
    void putDict() {
        // given
        DictPutRequestDto dto = DictPutRequestDto.builder()
                .summary("????????????")
                .content("????????????")
                .build();

        // when
        ResponseDto<DictPutResponseDto> result = dictController.putDict(userDetails, dict.getDictId(), dto);

        // then
        assertEquals(HttpStatus.OK.toString(), result.getStatus());
        assertEquals("?????? ??????", result.getData().getResult());
    }

    @Nested
    @DisplayName("?????? ?????????")
    class LikeDict {
        @Test
        @DisplayName("?????????")
        void successLike(){
            // given

            // when
            ResponseDto<DictLikeResponseDto> result = dictController.likeDict(userDetails, dict.getDictId());

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().isResult());
        }

        @Test
        @DisplayName("????????? ??????")
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
    @DisplayName("?????? ?????? ?????? ????????????")
    void dictHistory(){
        // given

        // when
        ResponseDto<DictHistoryResponseDto> result = dictController.getDictHistory(dict.getDictId());

        // then
        assertEquals(HttpStatus.OK.toString(), result.getStatus());
        assertEquals(dict.getDictId(), result.getData().getDictId());
    }

    @Nested
    @DisplayName("????????? ??? ?????? ??????")
    class BestMeme{
        @Test
        @DisplayName("????????? ??????")
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
        @DisplayName("???????????? ??????")
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
    @DisplayName("?????? ?????? ????????????")
    class SearchResult{
        @Test
        @DisplayName("????????? ??????")
        void getSearchResultUser(){
            // given

            // when
            ResponseDto<DictSearchResponseDto> result = dictController.getSearchResult(token, dict.getDictName(), 0, 10);

            // then
            assertEquals(HttpStatus.OK.toString(), result.getStatus());
            assertTrue(result.getData().getDictResult().size() > 0);
        }

        @Test
        @DisplayName("???????????? ??????")
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
