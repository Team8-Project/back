package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.rank.RankDictAllTimeResponseDto;
import com.teamproj.backend.dto.rank.RankResponseDto;
import com.teamproj.backend.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    @GetMapping("/api/dict/rank/week")
    public ResponseDto<List<RankResponseDto>> getRankWeek(){
        return ResponseDto.<List<RankResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(rankService.getRank(7))
                .build();
    }

    @GetMapping("/api/dict/rank/month")
    public ResponseDto<List<RankResponseDto>> getRankMonth(){
        return ResponseDto.<List<RankResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(rankService.getRank(30))
                .build();
    }

    @GetMapping("/api/dict/rank/allTimeDict")
    public ResponseDto<List<RankDictAllTimeResponseDto>> getAllTimeDict(@RequestHeader(value = "Authorization", required = false) String token){
        return ResponseDto.<List<RankDictAllTimeResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(rankService.getAllTimeDictRank(token))
                .build();
    }
}
