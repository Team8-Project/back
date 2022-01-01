package com.teamproj.backend.dto.board.BoardMemeBest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BoardMemeBestResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String username;
    private String profileImageUrl;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int likeCnt;
}
