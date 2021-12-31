package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.board.BoardHashTag.BoardHashTagResponseDto;
import com.teamproj.backend.service.BoardHashTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardHashTagController {
    private final BoardHashTagService boardHashTagService;

    @GetMapping("/api/board/hashTag")
    public ResponseDto<BoardHashTagResponseDto> getRecommendHashTag() {

        return ResponseDto.<BoardHashTagResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 추천 해시태그 받기 요청")
                .data(boardHashTagService.getRecommendHashTag())
                .build();
    }
}
