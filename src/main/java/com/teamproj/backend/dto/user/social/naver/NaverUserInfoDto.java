package com.teamproj.backend.dto.user.social.naver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NaverUserInfoDto {
    private String id;
    private String nickname;
    private String profileImage;
}
