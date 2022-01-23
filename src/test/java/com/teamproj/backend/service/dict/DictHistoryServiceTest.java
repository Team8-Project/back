package com.teamproj.backend.service.dict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.LoginRequestDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.DictPutRequestDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryRecentResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
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

import java.util.Objects;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
class DictHistoryServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;
    @Autowired
    private DictService dictService;
    @Autowired
    private DictHistoryService dictHistoryService;
    @Autowired
    private DictRepository dictRepository;

    // BeforeEach Data
    Dict dict;
    Long dictId;
    String title;
    String content;
    String summary;

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

        // 사전 초기 역사 데이터 주입(데이터 수정)
        DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                .content("변경")
                .summary("변경")
                .build();
        dictService.putDict(userDetails, dictId, dictPutRequestDto);
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
    @DisplayName("용어사전 수정 내역")
    class getDictHistory {
        @Nested
        @DisplayName("성공")
        class Success {
            @Test
            @DisplayName("롤백 기록이 존재하지 않는 수정 내역")
            void getDictHistory_success_login() {
                // given

                // when
                DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);

                // then
                assertEquals(dictId, dictHistoryResponseDto.getDictId());
                assertEquals(dict.getDictName(), dictHistoryResponseDto.getTitle());
                assertEquals(jwtAuthenticateProcessor.getUser(userDetails).getNickname(), dictHistoryResponseDto.getFirstWriter());
                assertEquals(1, dictHistoryResponseDto.getHistory().size());
            }
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사전의 수정 내역 열람 시도")
        void getDictHistory_fail_non_exist_dict() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> dictHistoryService.getDictHistory(0L)
            );

            // then
            assertEquals(NOT_EXIST_DICT, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("용어사전 수정 내역 상세")
    class getDictHistoryDetail {
        @Test
        @DisplayName("성공")
        void getDictHistoryDetail_success_login() {
            // given
            DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
            DictHistoryRecentResponseDto dictHistoryRecentResponseDto = dictHistoryResponseDto.getHistory().get(0);

            // when
            DictHistoryDetailResponseDto dictHistoryDetailResponseDto = dictHistoryService.getDictHistoryDetail(dictHistoryRecentResponseDto.getHistoryId());

            // then
            assertEquals(dict.getDictName(), dictHistoryDetailResponseDto.getTitle());
            assertEquals(jwtAuthenticateProcessor.getUser(userDetails).getNickname(), dictHistoryDetailResponseDto.getModifier());
            assertEquals(content, dictHistoryDetailResponseDto.getContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수정 내역 열람 시도")
        void getDictHistory_fail_non_exist_dict() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> dictHistoryService.getDictHistoryDetail(0L)
            );

            // then
            assertEquals(NOT_EXIST_DICT_HISTORY, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("용어사전 롤백")
    class RevertHistory {
        @Test
        @DisplayName("성공")
        void revertHistory_success() {
            // given
            DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
            Long historyId = dictHistoryResponseDto.getHistory().get(0).getHistoryId();

            // when
            dictHistoryService.revertDict(historyId, userDetails);
            dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);

            // then
            assertEquals(2, dictHistoryResponseDto.getHistory().size());
        }

        @Nested
        @DisplayName("실패")
        class RevertHistory_fail {
            @Test
            @DisplayName("비회원의 롤백 시도")
            void revertHistory_fail_non_login_user() {
                // given
                DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
                Long historyId = dictHistoryResponseDto.getHistory().get(0).getHistoryId();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictHistoryService.revertDict(historyId, null)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("존재하지 않는 히스토리로 롤백 시도")
            void revertHistory_fail_non_exist_history() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictHistoryService.revertDict(0L, userDetails)
                );

                // then
                assertEquals(NOT_EXIST_DICT_HISTORY, exception.getMessage());
            }
        }
    }
}