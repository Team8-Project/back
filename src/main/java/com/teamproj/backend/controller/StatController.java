package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService statService;

    @GetMapping("/api/stat/visitor")
    public ResponseDto<Long> statVisitor(){
        return ResponseDto.<Long>builder()
                .status(HttpStatus.OK.toString())
                .message("방문자 정보 수집")
                .data(statService.statVisitor())
                .build();
    }

    @GetMapping("/api/stat/quiz/{category}")
    public ResponseDto<Object> statQuizSolver(@PathVariable String category,
                                              @RequestParam int score){
        statService.statQuizSolver(category, score);
        return ResponseDto.builder()
                .status(HttpStatus.OK.toString())
                .message("정답 결과 정산..")
                .build();
    }
}
