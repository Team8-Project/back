package com.teamproj.backend.dto.board.BoardMemeBest;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
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

    public BoardMemeBestResponseDto(BoardMemeBestResponseDto dto, Long likeCnt, Boolean isLike){
        this.boardId = dto.getBoardId();
        this.thumbNail = dto.getThumbNail();
        this.title = dto.getTitle();
        this.username = dto.getUsername();
        this.profileImageUrl = dto.getProfileImageUrl();
        this.writer = dto.getWriter();
        this.content = dto.getContent();
        this.views = dto.getViews();
        this.likeCnt = likeCnt;
        this.isLike = isLike;
    }
}
