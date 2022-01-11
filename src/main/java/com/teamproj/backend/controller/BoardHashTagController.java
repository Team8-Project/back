package com.teamproj.backend.controller;

import com.teamproj.backend.dto.BoardHashTag.BoardHashTagSearchResponseDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.BoardHashTag.BoardHashTagResponseDto;
import com.teamproj.backend.service.BoardHashTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/api/board/hashTag/search")
    public ResponseDto<List<BoardHashTagSearchResponseDto>> boardHashTagSearch(@RequestParam String q) {

        return ResponseDto.<List<BoardHashTagSearchResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 해시태그 검색 요청")
                .data(boardHashTagService.getBoardHashTagSearch(q))
                .build();
    }
}
