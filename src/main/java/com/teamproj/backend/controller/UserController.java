package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.dto.kakao.KakaoUserResponseDto;
import com.teamproj.backend.service.KakaoUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final KakaoUserService kakaoUserService;

    @GetMapping("/api/user/kakao/callback")
    public ResponseEntity<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        return kakaoUserService.kakaoLogin(code);
    }
}
