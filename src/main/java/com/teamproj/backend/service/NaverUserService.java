package com.teamproj.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.user.social.kakao.KakaoUserInfoDto;
import com.teamproj.backend.dto.user.social.kakao.KakaoUserResponseDto;
import com.teamproj.backend.dto.user.social.naver.NaverUserInfoDto;
import com.teamproj.backend.dto.user.social.naver.NaverUserResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/*
    NAVER SOCIAL LOGIN SERVICE
    공식 문서 : https://developers.naver.com/docs/login/devguide/devguide.md
 */
@Service
@RequiredArgsConstructor
public class NaverUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.secret-key}")
    private String secretKey;

    public ResponseEntity<ResponseDto<NaverUserResponseDto>> naverLogin(String code, String state) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code, state);

        // 2. "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
        NaverUserInfoDto snsUserInfoDto = getNaverUserInfo(accessToken);

        // 3. "네이버 사용자 정보"로 필요시 회원가입  및 이미 같은 id가 있으면 기존회원으로 로그인
        User naverUser = registerNaverOrUpdateNaver(snsUserInfoDto);

        // 4. 강제 로그인 처리
        final String AUTH_HEADER = "Authorization";
        final String TOKEN_TYPE = "BEARER";

        String jwt_token = forceLogin(naverUser); // 로그인처리 후 토큰 받아오기
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, TOKEN_TYPE + " " + jwt_token);
        System.out.println("naver user's token : " + TOKEN_TYPE + " " + jwt_token);
        System.out.println("LOGIN SUCCESS!");

        NaverUserResponseDto naverUserResponseDto = NaverUserResponseDto.builder()
                .userId(naverUser.getId())
                .nickname(naverUser.getNickname())
                .build();
        ResponseDto<NaverUserResponseDto> responseDto = ResponseDto.<NaverUserResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("네이버 소셜 로그인 요청")
                .data(naverUserResponseDto)
                .build();
        return ResponseEntity.ok()
                .headers(headers)
                .body(responseDto);
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getAccessToken(String code, String state) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", secretKey);
        body.add("code", code);
        body.add("state", state);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                naverTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. "액세스 토큰"으로 "네이버 사용자 정보" 가져오기
    private NaverUserInfoDto getNaverUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.POST,
                naverUserInfoRequest,
                String.class
        );

        // HTTP 응답 받아오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        /*  Naver OAuth2 API 형식
            {
                "resultcode": "00",
                "message": "success",
                "response":
                {
                    "id": "GrzApdKssmguX3FcB8BG8QDUh9tKx45bXhEnOBxq_IU",
                    "nickname": "자비",
                    "profile_image": "https://ssl.pstatic.net/static/pwe/address/img_profile.png"
                }
            }
         */
        Long id = jsonNode.get("response").get("id").asLong();
        String nickname = jsonNode.get("response").get("nickname").asText();
        String profileImage = jsonNode.get("response").get("profile_image").asText();

        System.out.println("id : " + id);
        System.out.println("nickname : " + nickname);
        System.out.println("profileImage : " + profileImage);

        return NaverUserInfoDto.builder()
                .id(id)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }

    // 3. "네이버 사용자 정보"로 필요시 회원가입  및 이미 같은 id가 있으면 기존회원으로 로그인
    private User registerNaverOrUpdateNaver(NaverUserInfoDto naverUserInfoDto) {
        Optional<User> sameUser = userRepository.findByNaverId(naverUserInfoDto.getId());

        return sameUser.map(user -> updateNaverUser(user, naverUserInfoDto))
                       .orElseGet(() -> registerNaverUserIfNeeded(naverUserInfoDto));
    }

    private User registerNaverUserIfNeeded(NaverUserInfoDto naverUserInfoDto) {
        // DB 에 중복된 Naver Id 가 있는지 확인
        Long naverId = naverUserInfoDto.getId();
        Optional<User> naverUser = userRepository.findByNaverId(naverId);

        if (!naverUser.isPresent()) {
            // DB에 네이버 ID가 없을 시 회원가입
            // username: random UUID
            String username = "NAVER" + UUID.randomUUID();

            // username: naver nickname
            String nickname = naverUserInfoDto.getNickname();

            // profileImage : 프로필사진
            String profileImage = naverUserInfoDto.getProfileImage();

            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            User signUpUser = User.builder()
                    .username(username)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .profileImage(profileImage)
                    .naverId(naverId)
                    .build();

            naverUser = Optional.of(userRepository.save(signUpUser));
        }

        return naverUser.get();
    }

    private User updateNaverUser(User sameUser, NaverUserInfoDto snsUserInfoDto) {
        if (sameUser.getNaverId() == null) {
            System.out.println("중복");
            sameUser.setNaverId(snsUserInfoDto.getId());
            sameUser.setNickname(snsUserInfoDto.getNickname());
            userRepository.save(sameUser);
        }
        return sameUser;
    }

    // 4. 강제 로그인 처리
    private String forceLogin(User kakaoUser) {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .username(kakaoUser.getUsername())
                .password(kakaoUser.getPassword())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return JwtTokenUtils.generateJwtToken(userDetails);
    }
}
