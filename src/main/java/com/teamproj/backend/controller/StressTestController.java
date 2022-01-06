package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.StressTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StressTestController {
    private final StressTestService stressTestService;

    @GetMapping("/api/stress/board")
    public ResponseDto<String> stressToBoardWrite(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(stressTestService.boardWrite10000(userDetails))
                .build();
    }

    @GetMapping("/api/stress/dict")
    public ResponseDto<String> stressToDictWrite(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(stressTestService.dictWrite500(userDetails))
                .build();
    }
}
