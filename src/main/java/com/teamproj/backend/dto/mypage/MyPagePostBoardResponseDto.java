package com.teamproj.backend.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyPagePostBoardResponseDto {
    private Long boardId;
    private String title;
    private String username;
    private String profileImageUrl;
    private String thumbNail;
    private String category;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private int views;
    private int likeCnt;
    private int commentCnt;
}
