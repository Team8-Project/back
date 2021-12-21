package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.dto.kakao.KakaoUserResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.service.KakaoUserService;
import com.teamproj.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
