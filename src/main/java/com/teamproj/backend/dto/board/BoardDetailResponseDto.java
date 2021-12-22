package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

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
//    private List<commentRequestDto> commentList;
}
