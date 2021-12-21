package com.teamproj.backend.dto.kakao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KakaoUserInfoDto {
    Long id;
    String email;
    String nickname;
}
