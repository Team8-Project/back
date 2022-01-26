package com.teamproj.backend.dto.rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankDictAllTimeResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private String firstWriter;
    private LocalDateTime createdAt;
    private boolean isLike;
    private int likeCount;
}
