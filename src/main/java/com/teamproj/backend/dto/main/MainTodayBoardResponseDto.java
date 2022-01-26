package com.teamproj.backend.dto.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MainTodayBoardResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String writer;
}
