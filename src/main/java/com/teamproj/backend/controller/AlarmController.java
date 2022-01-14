package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.alarm.AlarmNavResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("/api/alarm/{alarmId}")
    public ResponseDto<AlarmNavResponseDto> navAlarm(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable Long alarmId) {

        return ResponseDto.<AlarmNavResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(alarmService.navAlarm(userDetails, alarmId))
                .build();
    }

    @GetMapping("/api/alarm/{alarmId}/read")
    public ResponseDto<String> readCheckAlarm(@PathVariable Long alarmId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(alarmService.readCheckAlarm(alarmId, userDetails))
                .build();
    }
}
