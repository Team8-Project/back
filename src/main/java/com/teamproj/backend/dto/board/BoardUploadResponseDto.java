package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardUploadResponseDto {
    private Long boardId;
    private String title;
    private String content;
    private String subject;
    private String category;
    private LocalDate createdAt;
}