package com.teamproj.backend.dto.board;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoardUploadRequestDto {
    private String title;
    private String content;
    private String subject;
    private String category;
}