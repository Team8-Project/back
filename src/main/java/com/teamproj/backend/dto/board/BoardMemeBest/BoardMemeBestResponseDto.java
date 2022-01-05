package com.teamproj.backend.dto.board.BoardMemeBest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BoardMemeBestResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String username;
    private String profileImageUrl;
    private String writer;
    private String content;
    private int views;
    private Long likeCnt;
    private Boolean isLike;
}
