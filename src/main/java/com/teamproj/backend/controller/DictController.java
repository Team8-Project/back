package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.DictHistoryService;
import com.teamproj.backend.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DictController {
    private final DictService dictService;
    private final DictHistoryService dictHistoryService;

    @GetMapping("/api/dict")
    public ResponseDto<List<DictResponseDto>> getDictList(@RequestHeader(value = "Authorization", required = false) String token,
                                                          @RequestParam int page,
                                                          @RequestParam int size) {
        if (token == null) {
            token = "";
        }
        System.out.println("token : " + token);
        return ResponseDto.<List<DictResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 목록 요청")
                .data(dictService.getDictList(page, size, token))
                .build();
    }

    @GetMapping("/api/dict/{dictId}")
    public ResponseDto<DictDetailResponseDto> getDictDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                                            @PathVariable Long dictId) {
        if (token == null) {
            token = "";
        }
        return ResponseDto.<DictDetailResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 상세")
                .data(dictService.getDictDetail(dictId, token))
                .build();
    }

    @PostMapping("/api/dict")
    public ResponseDto<DictPostResponseDto> postDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @RequestBody DictPostRequestDto dictPostRequestDto) {
        return ResponseDto.<DictPostResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 작성")
                .data(dictService.postDict(userDetails, dictPostRequestDto))
                .build();
    }

    @PutMapping("/api/dict/{dictId}")
    public ResponseDto<DictPutResponseDto> putDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @PathVariable Long dictId,
                                                   @RequestBody DictPutRequestDto dictPutRequestDto) {
        return ResponseDto.<DictPutResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 수정")
                .data(dictService.putDict(userDetails, dictId, dictPutRequestDto))
                .build();
    }

    @GetMapping("/api/dict/{dictId}/like")
    public ResponseDto<DictLikeResponseDto> likeDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable Long dictId) {
        return ResponseDto.<DictLikeResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 좋아요")
                .data(dictService.likeDict(userDetails, dictId))
                .build();
    }

    @GetMapping("/api/dict/{dictId}/history")
    public ResponseDto<DictHistoryResponseDto> getDictHistory(@PathVariable Long dictId) {
        return ResponseDto.<DictHistoryResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 역사 목록")
                .data(dictHistoryService.getDictHistory(dictId))
                .build();
    }

    @GetMapping("/api/dict/history/{historyId}")
    public ResponseDto<DictHistoryDetailResponseDto> getDictHistoryDetail(@PathVariable Long historyId) {
        return ResponseDto.<DictHistoryDetailResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 역사 상세")
                .data(dictHistoryService.getDictHistoryDetail(historyId))
                .build();
    }

    @GetMapping("/api/dict/revert/{historyId}")
    public ResponseDto<DictRevertResponseDto> revertDict(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable Long historyId) {
        return ResponseDto.<DictRevertResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 롤백")
                .data(dictHistoryService.revertDict(historyId, userDetails))
                .build();
    }

    @GetMapping("/api/searchInfo/dict")
    public ResponseDto<List<String>> getSearchInfo() {
        return ResponseDto.<List<String>>builder()
                .status(HttpStatus.OK.toString())
                .message("추천 검색어 요청")
                .data(dictService.getSearchInfo())
                .build();
    }

    @GetMapping("/api/dict/search")
    public ResponseDto<List<DictSearchResultResponseDto>> getSearchResult(@RequestHeader(value = "Authorization", required = false) String token,
                                                                          @RequestParam String q,
                                                                          @RequestParam int page,
                                                                          @RequestParam int size) {
        return ResponseDto.<List<DictSearchResultResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 검색어 : " + q)
                .data(dictService.getSearchResult(token, q, page, size))
                .build();
    }
}