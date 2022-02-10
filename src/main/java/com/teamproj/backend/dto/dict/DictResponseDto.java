package com.teamproj.backend.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String firstWriter;
    private LocalDateTime createdAt;
    private boolean isLike;
    private int likeCount;

    public DictResponseDto(Long dictId, String title, String summary, String firstWriter, LocalDateTime createdAt, int likeCount) {
        this.dictId = dictId;
        this.title = title;
        this.summary = summary;
        this.firstWriter = firstWriter;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
    }
}
