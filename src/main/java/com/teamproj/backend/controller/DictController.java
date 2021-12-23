package com.teamproj.backend.controller;

import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.DictHistoryService;
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
    private final DictHistoryService dictHistoryService;

    @GetMapping("/api/dict")
    public ResponseEntity<List<DictResponseDto>> getDictList(@RequestHeader(value="Authorization", required = false) String token,
                                                             @RequestParam int page,
                                                             @RequestParam int size){
        if(token == null){
            token = "";
        }
        System.out.println("token : " + token);
        return ResponseEntity.ok()
                .body(dictService.getDictList(page, size, token));
    }

    @GetMapping("/api/dict/{dictId}")
    public ResponseEntity<DictDetailResponseDto> getDictDetail(@RequestHeader(value="Authorization", required = false) String token,
                                                               @PathVariable Long dictId){
        if(token == null){
            token = "";
        }
        return ResponseEntity.ok()
                .body(dictService.getDictDetail(dictId, token));
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

    @GetMapping("/api/dict/{dictId}/history")
    public ResponseEntity<DictHistoryResponseDto> getDictHistory(@PathVariable Long dictId){
        return ResponseEntity.ok()
                .body(dictHistoryService.getDictHistory(dictId));
    }

    @GetMapping("/api/dict/history/{historyId}")
    public ResponseEntity<DictHistoryDetailResponseDto> getDictHistoryDetail(@PathVariable Long historyId){
        return ResponseEntity.ok()
                .body(dictHistoryService.getDictHistoryDetail(historyId));
    }

    @GetMapping("/api/dict/revert/{historyId}")
    public ResponseEntity<DictRevertResponseDto> revertDict(@PathVariable Long historyId){
        return ResponseEntity.ok()
                .body(dictHistoryService.revertDict(historyId));
    }
}
