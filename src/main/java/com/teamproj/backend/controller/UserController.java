package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpCheckResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.dto.user.social.kakao.KakaoUserResponseDto;
import com.teamproj.backend.dto.user.social.naver.NaverUserResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserInfoResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserNicknameModifyRequestDto;
import com.teamproj.backend.dto.user.userInfo.UserNicknameModifyResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.KakaoUserService;
import com.teamproj.backend.service.NaverUserService;
import com.teamproj.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final KakaoUserService kakaoUserService;
    private final NaverUserService naverUserService;
    private final UserService userService;

    @PostMapping("/api/signup")
    public ResponseDto<SignUpResponseDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        return ResponseDto.<SignUpResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("회원가입 요청")
                .data(userService.signUp(signUpRequestDto))
                .build();
    }

    @GetMapping("/api/user/kakao/callback")
    public ResponseDto<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {

        return ResponseDto.<KakaoUserResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("카카오 소셜 로그인 요청")
                .data(kakaoUserService.kakaoLogin(code))
                .build();
    }

    @GetMapping("/api/user/naver/callback")
    public ResponseEntity<ResponseDto<NaverUserResponseDto>> naverLogin(@RequestParam String code,
                                                           @RequestParam String state) throws JsonProcessingException {
        return naverUserService.naverLogin(code, state);
    }


    @GetMapping("/api/userInfo")
    public ResponseDto<UserInfoResponseDto> userInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseDto.<UserInfoResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사용자 정보 요청 기능 수행")
                .data(userService.getUserInfo(userDetails))
                .build();
    }

    @GetMapping("/api/signup/username")
    public ResponseEntity<SignUpCheckResponseDto> usernameValidCheck(@RequestParam String username) {
        return ResponseEntity.ok()
                .body(userService.usernameValidCheck(username));
    }

    @GetMapping("/api/signup/nickname")
    public ResponseEntity<SignUpCheckResponseDto> nicknameValidCheck(@RequestParam String nickname) {
        return ResponseEntity.ok()
                .body(userService.nicknameValidCheck(nickname));
    }

    @PostMapping("/api/user/nickname")
    public ResponseDto<UserNicknameModifyResponseDto> nicknameModify(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                     @RequestBody UserNicknameModifyRequestDto userNicknameModifyRequestDto) {
        return ResponseDto.<UserNicknameModifyResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("닉네임 변경 요청")
                .data(userService.nicknameModify(userDetails, userNicknameModifyRequestDto))
                .build();
    }
}
