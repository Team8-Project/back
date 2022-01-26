package com.teamproj.backend.dto.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MainMemeImageResponseDto {
    private Long boardId;
    private String imageUrl;
}
