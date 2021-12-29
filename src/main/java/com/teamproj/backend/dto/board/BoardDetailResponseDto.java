package com.teamproj.backend.dto.board;

import com.teamproj.backend.dto.comment.CommentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardDetailResponseDto {
    private Long boardId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int likeCnt;
    private Boolean isLike;
    private List<CommentResponseDto> commentList;
}
