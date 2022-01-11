package com.teamproj.backend.dto.BoardHashTag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardHashTagSearchResponseDto {
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
    private List<String> hashTags;
}
