package com.teamproj.backend.dto.user.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDto {
    private Long userId;
    private String username;
    private String nickname;
}
