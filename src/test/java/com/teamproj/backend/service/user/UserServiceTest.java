package com.teamproj.backend.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.LoginRequestDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.user.login.LoginResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpCheckResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserInfoResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserNicknameModifyRequestDto;
import com.teamproj.backend.dto.user.userInfo.UserNicknameModifyResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.UserService;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class UserServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;

    @Autowired
    private UserService userService;

    String username;
    String nickname;
    String password;
    String passwordCheck;


    UserDetailsImpl userDetails;
    User user;
    String token;

    @BeforeEach
    void setup() throws JsonProcessingException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 사용자 초기데이터 주입
        username = "iamtester3";
        nickname = "테스터test";
        password = "a1234567";
        passwordCheck = password;

        SignUpRequestDto dto = SignUpRequestDto.builder()
                .username("iamtester")
                .nickname("테스터tester")
                .password("a1234567")
                .passwordCheck("a1234567")
                .build();
        userService.signUp(dto);

        token = logIn("test", "a1234567");
        userDetails = jwtAuthenticateProcessor.forceLogin(token);
        user = jwtAuthenticateProcessor.getUser(userDetails);
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
    @DisplayName("회원가입")
    class SignUp {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            SignUpRequestDto dto = SignUpRequestDto.builder()
                    .username(username)
                    .nickname(nickname)
                    .password(password)
                    .passwordCheck(passwordCheck)
                    .build();

            // when
            SignUpResponseDto result = userService.signUp(dto);

            // then
            assertEquals(username, result.getUsername());
            assertEquals(nickname, result.getNickname());
        }

        @Nested
        @DisplayName("실패")
        class Fail {
            @Nested
            @DisplayName("username 오류")
            class FailUsername {
                @Test
                @DisplayName("3자 미만이거나 16자 초과인 아이디")
                void tooShortOrLong() {
                    //given
                    SignUpRequestDto dtoShort = SignUpRequestDto.builder()
                            .username("hi")
                            .nickname(nickname)
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    SignUpRequestDto dtoLong = SignUpRequestDto.builder()
                            .username("hihihihihihihihih")
                            .nickname(nickname)
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exceptionShort = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoShort));
                    Exception exceptionLong = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoLong));

                    // then
                    assertEquals(ILLEGAL_USERNAME_LENGTH, exceptionShort.getMessage());
                    assertEquals(ILLEGAL_USERNAME_LENGTH, exceptionLong.getMessage());
                }

                @Test
                @DisplayName("잘못된 아이디 형식")
                void invalidFormat() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username("!@!@#!@$")
                            .nickname(nickname)
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(ILLEGAL_USERNAME_FORMAT, exception.getMessage());
                }

                @Test
                @DisplayName("중복된 아이디")
                void exists() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username("iamtester")
                            .nickname(nickname)
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(EXIST_USERNAME, exception.getMessage());
                }
            }

            @Nested
            @DisplayName("nickname 오류")
            class FailNickname {
                @Test
                @DisplayName("2자 미만이거나 10자 초과인 닉네임")
                void tooShortOrLong() {
                    //given
                    SignUpRequestDto dtoShort = SignUpRequestDto.builder()
                            .username(username)
                            .nickname("h")
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    SignUpRequestDto dtoLong = SignUpRequestDto.builder()
                            .username(username)
                            .nickname("hihihihihih")
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exceptionShort = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoShort));
                    Exception exceptionLong = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoLong));

                    // then
                    assertEquals(ILLEGAL_NICKNAME_LENGTH, exceptionShort.getMessage());
                    assertEquals(ILLEGAL_NICKNAME_LENGTH, exceptionLong.getMessage());
                }

                @Test
                @DisplayName("잘못된 닉네임 형식")
                void invalidFormat() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username(username)
                            .nickname("!@#!@$!@")
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(ILLEGAL_NICKNAME_FORMAT, exception.getMessage());
                }

                @Test
                @DisplayName("중복된 닉네임")
                void exists() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username(username)
                            .nickname("테스터tester")
                            .password(password)
                            .passwordCheck(passwordCheck)
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(EXIST_NICKNAME, exception.getMessage());
                }
            }

            @Nested
            @DisplayName("password 오류")
            class FailPassword {
                @Test
                @DisplayName("6자 미만이거나 16자 초과인 비밀번호")
                void tooShortOrLong() {
                    //given
                    SignUpRequestDto dtoShort = SignUpRequestDto.builder()
                            .username(username)
                            .nickname(nickname)
                            .password("12345")
                            .passwordCheck("12345")
                            .build();

                    SignUpRequestDto dtoLong = SignUpRequestDto.builder()
                            .username(username)
                            .nickname(nickname)
                            .password("12345678901234567")
                            .passwordCheck("12345678901234567")
                            .build();

                    // when
                    Exception exceptionShort = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoShort));
                    Exception exceptionLong = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dtoLong));

                    // then
                    assertEquals(ILLEGAL_PASSWORD_LENGTH, exceptionShort.getMessage());
                    assertEquals(ILLEGAL_PASSWORD_LENGTH, exceptionLong.getMessage());
                }

                @Test
                @DisplayName("잘못된 비밀번호 형식")
                void invalidFormat() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username(username)
                            .nickname(nickname)
                            .password("tester")
                            .passwordCheck("tester")
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(ILLEGAL_PASSWORD_FORMAT, exception.getMessage());
                }

                @Test
                @DisplayName("비밀번호 확인과 일치하지 않음")
                void notMatching() {
                    //given
                    SignUpRequestDto dto = SignUpRequestDto.builder()
                            .username(username)
                            .nickname(nickname)
                            .password("a1234567")
                            .passwordCheck("a12345678")
                            .build();

                    // when
                    Exception exception = assertThrows(IllegalArgumentException.class,
                            () -> userService.signUp(dto));

                    // then
                    assertEquals(ILLEGAL_MATCHING_PASSWORD_PASSWORD_CHECK, exception.getMessage());
                }
            }
        }
    }

    @Nested
    @DisplayName("중복 검사")
    class ValidCheck {
        @Nested
        @DisplayName("username 중복 검사")
        class Username {
            @Test
            @DisplayName("사용 가능")
            void ok() {
                // given
                String username = "test1234";

                // when
                SignUpCheckResponseDto result = userService.usernameValidCheck(username);

                // then
                assertTrue(result.isResult());
            }

            @Test
            @DisplayName("사용 불가")
            void no() {
                // given
                String username = "iamtester";

                // when
                SignUpCheckResponseDto result = userService.usernameValidCheck(username);

                // then
                assertFalse(result.isResult());
            }
        }

        @Nested
        @DisplayName("nickname 중복 검사")
        class Nickname {
            @Test
            @DisplayName("사용 가능")
            void ok() {
                // given
                String nickname = "test1234";

                // when
                SignUpCheckResponseDto result = userService.nicknameValidCheck(nickname);

                // then
                assertTrue(result.isResult());
            }

            @Test
            @DisplayName("사용 불가")
            void no() {
                // given
                String nickname = "테스터tester";

                // when
                SignUpCheckResponseDto result = userService.nicknameValidCheck(nickname);

                // then
                assertFalse(result.isResult());
            }
        }
    }

    @Nested
    @DisplayName("닉네임 수정")
    class ChangeNickname {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            UserNicknameModifyRequestDto dto = UserNicknameModifyRequestDto.builder()
                    .nickname("toast324")
                    .build();

            // when
            UserNicknameModifyResponseDto result = userService.nicknameModify(userDetails, dto);

            // then
            assertEquals("변경 완료", result.getResult());
        }
    }

    @Nested
    @DisplayName("사용자명 요청")
    class UserInfo{
        @Test
        @DisplayName("성공")
        void success(){
            // given

            // when
            UserInfoResponseDto result = userService.getUserInfo(userDetails);

            // then
            assertEquals(user.getUsername(), result.getUsername());
            assertEquals(user.getNickname(), result.getNickname());
            assertEquals(user.getProfileImage(), result.getProfileImage());
        }
    }
}
