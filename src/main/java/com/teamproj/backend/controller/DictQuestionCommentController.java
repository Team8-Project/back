package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.DictQuestionCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DictQuestionCommentController {
    private final DictQuestionCommentService dictQuestionCommentService;

    @PostMapping("/api/dict/{questionId}/comment")
    public ResponseDto<CommentPostResponseDto> postComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long questionId,
                                                           @RequestBody CommentPostRequestDto commentPostRequestDto) {
        return ResponseDto.<CommentPostResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 작성")
                .data(dictQuestionCommentService.postComment(userDetails, questionId, commentPostRequestDto))
                .build();
    }

    @DeleteMapping("/api/dict/comment/{commentId}")
    public ResponseDto<CommentDeleteResponseDto> deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @PathVariable Long commentId) {
        return ResponseDto.<CommentDeleteResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 삭제")
                .data(dictQuestionCommentService.deleteComment(userDetails, commentId))
                .build();
    }
}
