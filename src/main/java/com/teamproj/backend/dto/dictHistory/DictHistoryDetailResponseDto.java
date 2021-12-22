package com.teamproj.backend.dto.dictHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictHistoryDetailResponseDto {
    private Long dictId;
    private String title;
    private String firstWriter;
    private String modifier;
    private String content;
    private LocalDate createdAt;
}
