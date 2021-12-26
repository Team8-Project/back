package com.teamproj.backend.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DictSearchResultResponseDto {
    private Long dictId;
    private String title;
    private String meaning;
    private boolean isLike;
    private int likeCount;
}
