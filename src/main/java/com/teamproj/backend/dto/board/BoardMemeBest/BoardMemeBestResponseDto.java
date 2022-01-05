package com.teamproj.backend.dto.board.BoardMemeBest;

import lombok.*;

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
