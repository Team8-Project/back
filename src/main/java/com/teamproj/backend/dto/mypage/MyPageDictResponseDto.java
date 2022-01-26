package com.teamproj.backend.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MyPageDictResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private int likeCount;
    private String firstWriter;
    private String recentWriter;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
