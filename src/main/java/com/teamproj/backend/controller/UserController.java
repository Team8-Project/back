package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.dto.kakao.KakaoUserResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpCheckResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserInfoResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.KakaoUserService;
import com.teamproj.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final KakaoUserService kakaoUserService;
    private final UserService userService;

    @PostMapping("/api/signup")
    public ResponseEntity<SignUpResponseDto> signup(@RequestBody SignUpRequestDto signUpRequestDto){
        return ResponseEntity.ok()
                .body(userService.signUp(signUpRequestDto));
    }

    @GetMapping("/api/user/kakao/callback")
    public ResponseEntity<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        return kakaoUserService.kakaoLogin(code);
    }

    @GetMapping("/api/userInfo")
    public ResponseEntity<UserInfoResponseDto> userInfo(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok()
                .body(userService.getUserInfo(userDetails));
    }

    @GetMapping("/api/signup/username")
    public ResponseEntity<SignUpCheckResponseDto> usernameValidCheck(@RequestParam String username){
        return ResponseEntity.ok()
                .body(userService.usernameValidCheck(username));
    }

    @GetMapping("/api/signup/nickname")
    public ResponseEntity<SignUpCheckResponseDto> nicknameValidCheck(@RequestParam String nickname){
        return ResponseEntity.ok()
                .body(userService.nicknameValidCheck(nickname));
    }
}