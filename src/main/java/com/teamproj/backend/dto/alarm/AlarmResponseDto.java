package com.teamproj.backend.dto.alarm;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmResponseDto {
    private Long alarmId;
    private String alarmType;
    private boolean checked;
    private Long navId;
    private String username;
    private String nickname;
}
