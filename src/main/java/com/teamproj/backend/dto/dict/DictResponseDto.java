package com.teamproj.backend.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private boolean isLike;
    private int likeCount;
}
