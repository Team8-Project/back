package com.teamproj.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.teamproj.backend.dto.kakao.HeaderDto;
import com.teamproj.backend.service.KakaoUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final KakaoUserService kakaoUserService;

    @GetMapping("/user/kakao/callback")
    public String kakaoLogin(
            @RequestParam String code
    ) throws JsonProcessingException {
        System.out.println(code);
        kakaoUserService.kakaoLogin(code);
        return "test";
    }
}
