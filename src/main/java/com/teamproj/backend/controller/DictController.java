package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dict.mymeme.DictMyMemeResponseDto;
import com.teamproj.backend.dto.dict.search.DictSearchResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.StatService;
import com.teamproj.backend.service.dict.DictHistoryService;
import com.teamproj.backend.service.dict.DictService;
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
    private final StatService statService;

    @GetMapping("/api/dict")
    public ResponseDto<List<DictResponseDto>> getDictList(@RequestHeader(value = "Authorization", required = false) String token,
                                                          @RequestParam int page,
                                                          @RequestParam int size) {
        return ResponseDto.<List<DictResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 목록 요청")
                .data(dictService.getDictList(page, size, token))
                .build();
    }

    @GetMapping("/api/myMeme/dict")
    public ResponseDto<List<DictMyMemeResponseDto>> getMyMeme(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseDto.<List<DictMyMemeResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictService.getMyMeme(userDetails))
                .build();
    }

    @PostMapping("/api/check/dict")
    public ResponseDto<DictNameCheckResponseDto> checkDictName(@RequestBody DictNameCheckRequestDto dictNameCheckRequestDto){
        return ResponseDto.<DictNameCheckResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 이름 중복체크")
                .data(dictService.checkDictName(dictNameCheckRequestDto))
                .build();
    }

    @GetMapping("/api/count/dict")
    public ResponseDto<Long> getDictTotalCount(@RequestParam(required = false) String q) {
        return ResponseDto.<Long>builder()
                .status(HttpStatus.OK.toString())
                .message("사전 총 개수 요청")
                .data(dictService.getDictTotalCount(q))
                .build();
    }

    @GetMapping("/api/dict/{dictId}")
    public ResponseDto<DictDetailResponseDto> getDictDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                                            @PathVariable Long dictId) {
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

    @GetMapping("/api/bestDict/dict")
    public ResponseDto<List<DictBestResponseDto>> getBestDict(@RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseDto.<List<DictBestResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("오늘의 밈 카드 요청")
                .data(dictService.getBestDict(token))
                .build();
    }

    @GetMapping("/api/dict/search")
    public ResponseDto<DictSearchResponseDto> getSearchResult(@RequestHeader(value = "Authorization", required = false) String token,
                                                              @RequestParam String q,
                                                              @RequestParam int page,
                                                              @RequestParam int size) {
        return ResponseDto.<DictSearchResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("검색어 : " + q)
                .data(dictService.getSearchResult(token, q, page, size))
                .build();
    }
}