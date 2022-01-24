package com.teamproj.backend.dto.user.userInfo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNicknameModifyRequestDto {
    private String nickname;
}
