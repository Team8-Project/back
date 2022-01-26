package com.teamproj.backend.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyPageProfileImageModifyResponseDto {
    private String profileImageUrl;
}
