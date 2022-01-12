package com.teamproj.backend.dto.rank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankResponseDto {
    private Long userId;
    private String profileImage;
    private String nickname;
    private Long postCount;
}
