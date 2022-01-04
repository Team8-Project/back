package com.teamproj.backend.dto.user.social.kakao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KakaoUserInfoDto {
    Long id;
    String nickname;
    String profileImage;
}
