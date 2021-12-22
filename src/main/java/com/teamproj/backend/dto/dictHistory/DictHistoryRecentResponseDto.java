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
public class DictHistoryRecentResponseDto {
    private Long historyId;
    private String writer;
    private LocalDate createdAt;
}
