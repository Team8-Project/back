package com.teamproj.backend.controller;

import com.teamproj.backend.Repository.board.BoardTodayLikeRepository;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.main.MainPageResponseDto;
import com.teamproj.backend.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;
    private final BoardTodayLikeRepository boardTodayLikeRepository;

    @GetMapping("/api/main")
    public ResponseDto<MainPageResponseDto> getMainPageElements(@RequestHeader(value="Authorization", required = false) String token){
        if(token==null){
            token = "";
        }
        return ResponseDto.<MainPageResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("메인페이지 데이터 요청")
                .data(mainService.getMainPageElements(token))
                .build();
    }
}
