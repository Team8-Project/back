package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String content;
    private String username;
    private String profileImageUrl;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int likeCnt;
    private int commentCnt;
    private Boolean isLike;
    private List<String> hashTags;
}
