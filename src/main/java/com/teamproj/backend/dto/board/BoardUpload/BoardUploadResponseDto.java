package com.teamproj.backend.dto.board.BoardUpload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardUploadResponseDto {
    private Long boardId;
    private String title;
    private String content;
    private String category;
    private String thumbNail;
    private LocalDateTime createdAt;
}