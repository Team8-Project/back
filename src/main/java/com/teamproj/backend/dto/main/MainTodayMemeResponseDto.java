package com.teamproj.backend.dto.main;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MainTodayMemeResponseDto {
    private Long dictId;
    private String dictName;
}
