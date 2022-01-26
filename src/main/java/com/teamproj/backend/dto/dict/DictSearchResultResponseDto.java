package com.teamproj.backend.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DictSearchResultResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private String firstWriter;
    private LocalDateTime createdAt;
    private boolean isLike;
    private int likeCount;
}
