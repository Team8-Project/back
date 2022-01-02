package com.teamproj.backend.dto.user.social.naver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverUserResponseDto {
    private String token;
    private String result;
}
