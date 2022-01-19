package com.teamproj.backend.dto.board.BoardSearch;

import com.teamproj.backend.dto.comment.CommentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardSearchResponseDto {
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
}
