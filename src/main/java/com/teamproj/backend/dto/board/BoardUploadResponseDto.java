package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class BoardUploadResponseDto {
    private Long boardId;
    private String title;
    private String content;
    private String category;
    private String thumbNail;
    private LocalDate createdAt;
}