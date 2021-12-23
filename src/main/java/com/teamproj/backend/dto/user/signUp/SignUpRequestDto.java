package com.teamproj.backend.dto.user.signUp;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    private String username;
    private String nickname;
    private String password;
    private String passwordCheck;
}
