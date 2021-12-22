package com.teamproj.backend.dto.board;

import com.teamproj.backend.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardDetailResponseDto {
    private Long boardId;
    private String title;
    private String content;
    private String writer;
    private LocalDate createdAt;
    private String subject;
    private int views;
    private int likeCnt;
    private List<Comment> commentList;
}
