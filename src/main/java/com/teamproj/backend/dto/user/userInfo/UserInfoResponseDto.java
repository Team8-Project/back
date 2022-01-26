package com.teamproj.backend.dto.user.userInfo;

import com.teamproj.backend.dto.alarm.AlarmResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private String username;
    private String nickname;
    private String profileImage;
    private List<AlarmResponseDto> alarm;
}
