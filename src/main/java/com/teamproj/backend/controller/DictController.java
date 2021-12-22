package com.teamproj.backend.controller;

import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DictController {
    private final DictService dictService;

    @GetMapping("/api/dict")
    public ResponseEntity<List<DictResponseDto>> getDictList(@RequestHeader(value="Authorization", required = false) String token,
                                                             @RequestParam int page,
                                                             @RequestParam int size){
        if(token == null){
            token = "";
        }
        System.out.println("token : " + token);
        return ResponseEntity.ok()
                .body(dictService.getDicts(page, size, token));
    }

    @GetMapping("/api/dict/{dictId}")
    public ResponseEntity<DictDetailResponseDto> getDictDetail(@PathVariable Long dictId){
        return ResponseEntity.ok()
                .body(dictService.getDictDetail(dictId));
    }

    @PostMapping("/api/dict")
    public ResponseEntity<DictPostResponseDto> postDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestBody DictPostRequestDto dictPostRequestDto){
        return ResponseEntity.ok()
                .body(dictService.postDict(userDetails, dictPostRequestDto));
    }

    @PutMapping("/api/dict/{dictId}")
    public ResponseEntity<DictPutResponseDto> putDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                      @PathVariable Long dictId,
                                                      @RequestBody DictPutRequestDto dictPutRequestDto){
        return ResponseEntity.ok()
                .body(dictService.putDict(userDetails, dictId, dictPutRequestDto));
    }

    @GetMapping("/api/dict/{dictId}/like")
    public ResponseEntity<DictLikeResponseDto> likeDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long dictId){
        return ResponseEntity.ok()
                .body(dictService.likeDict(userDetails, dictId));
    }
}
