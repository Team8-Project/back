package com.teamproj.backend.dto.board.BoardDetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardDetailResponseDto {
    private Long boardId;
    private String title;
    private String username;
    private String content;
    private String writer;
    private String profileImageUrl;
    private String thumbNail;
    private LocalDateTime createdAt;
    private int views;
    private int likeCnt;
    private Boolean isLike;
//    private List<CommentResponseDto> commentList;
    private int commentCnt;
}
