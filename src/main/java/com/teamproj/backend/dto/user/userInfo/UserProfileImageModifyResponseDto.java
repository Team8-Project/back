package com.teamproj.backend.dto.user.userInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileImageModifyResponseDto {
    private String profileImageUrl;
}
