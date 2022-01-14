package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dict.question.DictQuestionResponseDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadRequestDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadResponseDto;
import com.teamproj.backend.dto.dict.question.detail.DictQuestionDetailResponseDto;
import com.teamproj.backend.dto.dict.question.update.DictQuestionUpdateRequestDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.dict.DictQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DictQuestionController {
    private final DictQuestionService dictQuestionService;

    @GetMapping("/api/dict/question")
    public ResponseDto<List<DictQuestionResponseDto>> getQuestion(@RequestParam("page") int page,
                                                                  @RequestParam("size") int size,
                                                                  @RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseDto.<List<DictQuestionResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.getQuestion(page, size, token))
                .build();
    }

    @GetMapping("/api/dict/question/{questionId}")
    public ResponseDto<DictQuestionDetailResponseDto> getQuestionDetail(@PathVariable Long questionId,
                                                                        @RequestHeader(value = "Authorization", required = false) String token) {
        return ResponseDto.<DictQuestionDetailResponseDto>builder()
                .data(dictQuestionService.getQuestionDetail(questionId, token))
                .build();
    }

    @PostMapping("/api/dict/question")
    public ResponseDto<DictQuestionUploadResponseDto> uploadQuestion(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                     @RequestPart DictQuestionUploadRequestDto dictQuestionUploadRequestDto,
                                                                     @RequestPart(value = "thumbNail", required = false) MultipartFile multipartFile) {
        return ResponseDto.<DictQuestionUploadResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.uploadQuestion(userDetails, dictQuestionUploadRequestDto, multipartFile))
                .build();
    }

    @PutMapping("/api/dict/question/{questionId}")
    public ResponseDto<String> putQuestion(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable Long questionId,
                                           @RequestPart DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto,
                                           @RequestPart(value = "thumbNail", required = false) MultipartFile multipartFile) {
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.updateQuestion(questionId, userDetails, dictQuestionUpdateRequestDto, multipartFile))
                .build();
    }

    @DeleteMapping("/api/dict/question/{questionId}")
    public ResponseDto<String> deleteQuestion(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable Long questionId) {
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.deleteQuestion(userDetails, questionId))
                .build();
    }

    @GetMapping("/api/dict/question/curiousToo/{questionId}")
    public ResponseDto<Boolean> curiousTooQuestion(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @PathVariable Long questionId) {
        return ResponseDto.<Boolean>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.curiousTooQuestion(userDetails, questionId))
                .build();
    }

    @GetMapping("/api/dict/question/select/{commentId}")
    public ResponseDto<String> selectAnswer(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long commentId){
        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.selectAnswer(userDetails, commentId))
                .build();
    }

    @GetMapping("/api/dict/question/count")
    public ResponseDto<Long> getQuestionCount(){
        return ResponseDto.<Long>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(dictQuestionService.getTotalQuestionCount())
                .build();
    }
}

