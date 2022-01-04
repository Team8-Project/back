package com.teamproj.backend.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.clientSecret}")
    private String clientSecret;

    public ResponseEntity<String> googleLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);


        // 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
//        KakaoUserInfoDto snsUserInfoDto = getKakaoUserInfo(accessToken);

        // 3. "구글 사용자 정보"로 필요시 회원가입  및 이미 같은 이메일이 있으면 기존회원으로 로그인
//        User kakaoUser = registerKakaoOrUpdateKakao(snsUserInfoDto);

        // 4. 강제 로그인 처리
        final String AUTH_HEADER = "Authorization";
        final String TOKEN_TYPE = "BEARER";

//        String jwt_token = forceLogin(); // 로그인처리 후 토큰 받아오기
//        HttpHeaders headers = new HttpHeaders();
//        headers.set(AUTH_HEADER, TOKEN_TYPE + " " + jwt_token);
//        KakaoUserResponseDto kakaoUserResponseDto = KakaoUserResponseDto.builder()
//                .result("로그인 성공")
//                .token(TOKEN_TYPE + " " + jwt_token)
//                .build();
//        System.out.println("kakao user's token : " + TOKEN_TYPE + " " + jwt_token);
//        System.out.println("LOGIN SUCCESS!");


        return null;
    }


    private String getAccessToken(String code) throws JsonProcessingException {
//        //HTTP Request를 위한 RestTemplate
//        RestTemplate restTemplate = new RestTemplate();
//
//        //Google OAuth Access Token 요청을 위한 파라미터 세팅
//        GoogleOAuthRequest googleOAuthRequestParam = GoogleOAuthRequest
//                .builder()
//                .clientId(clientId)
//                .clientSecret(clientSecret)
//                .code(code)
//                .redirectUri("http://localhost:8080/api/user/google/callback")
//                .grantType("authorization_code").build();
//
//        //JSON 파싱을 위한 기본값 세팅
//        //요청시 파라미터는 스네이크 케이스로 세팅되므로 Object mapper에 미리 설정해준다.
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//
//
//        //AccessToken 발급 요청
//        ResponseEntity<String> resultEntity  = restTemplate.postForEntity("https://oauth2.googleapis.com/token", googleOAuthRequestParam, String.class);
//
//
//        System.out.println(resultEntity.getBody());
//        String responseBody = resultEntity.getBody();
//
//        // 토큰 값
//        JsonNode jsonNode = mapper.readTree(responseBody);
//        String accessToken = jsonNode.get("access_token").asText();

        return null;
    }

    private String forceLogin() {
        return null;
    }
}
