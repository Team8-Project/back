package com.teamproj.backend.dto.dictHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictHistoryResponseDto {
    private Long dictId;
    private String title;
    private String firstWriter;
    private String firstWriterProfileImage;
    private LocalDateTime firstCreatedAt;
    private List<DictHistoryRecentResponseDto> history;
}
