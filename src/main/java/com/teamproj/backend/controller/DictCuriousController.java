package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dictCurious.DictCuriousPostRequestDto;
import com.teamproj.backend.dto.dictCurious.DictCuriousPostResponseDto;
import com.teamproj.backend.dto.dictCurious.DictCuriousResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.dict.DictCuriousService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DictCuriousController {
    private final DictCuriousService dictCuriousService;

    @GetMapping("/api/dict/curious")
    public ResponseDto<List<DictCuriousResponseDto>> getCurious(){
        return ResponseDto.<List<DictCuriousResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictCuriousService.getDictCurious())
                .build();
    }

    @PostMapping("/api/dict/curious")
    public ResponseDto<DictCuriousPostResponseDto> postCurious(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @RequestBody DictCuriousPostRequestDto dictCuriousPostRequestDto){
        return ResponseDto.<DictCuriousPostResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictCuriousService.postDictCurious(userDetails, dictCuriousPostRequestDto))
                .build();
    }

    @DeleteMapping("/api/dict/curious/{curiousId}")
    public ResponseDto<String> deleteCurious(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long curiousId){
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictCuriousService.deleteDictCurious(userDetails, curiousId))
                .build();
    }
}
