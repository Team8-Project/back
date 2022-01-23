package com.teamproj.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.dto.LoginRequestDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.mypage.MyPageResponseDto;
import com.teamproj.backend.dto.user.login.LoginResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.model.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class MyPageServiceTest {
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;

    @Autowired
    private UserService userService;
    @Autowired
    private MyPageService myPageService;

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
    @DisplayName("마이페이지 정보 조회")
    class Success {
        @Test
        @DisplayName("성공")
        void success() {
            // given

            // when
            MyPageResponseDto result = myPageService.myPage(userDetails);

            // then
            assertEquals(user.getId(), result.getUserId());
        }
    }
}
