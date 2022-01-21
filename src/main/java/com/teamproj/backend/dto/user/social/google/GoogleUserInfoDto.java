package com.teamproj.backend.dto.user.social.google;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GoogleUserInfoDto {
    String username;
    String nickname;
    String profileImage;
}
