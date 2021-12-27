package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/api/board/{boardId}/comment")
    public ResponseDto<List<CommentResponseDto>> getComments(@PathVariable Long boardId,
                                                             @RequestParam int page,
                                                             @RequestParam int size) {
        return ResponseDto.<List<CommentResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 조회 요청")
                .data(commentService.getCommentList(boardId, page, size))
                .build();
    }

    @PostMapping("/api/board/{boardId}/comment")
    public ResponseDto<CommentPostResponseDto> postComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long boardId,
                                                           @RequestBody CommentPostRequestDto commentPostRequestDto) {
        return ResponseDto.<CommentPostResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 작성")
                .data(commentService.postComment(userDetails, boardId, commentPostRequestDto))
                .build();
    }

    @PutMapping("/api/board/comment/{commentId}")
    public ResponseDto<CommentPutResponseDto> putComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable Long commentId,
                                                         @RequestBody CommentPutRequestDto commentPutRequestDto) {
        return ResponseDto.<CommentPutResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 수정")
                .data(commentService.putComment(userDetails, commentId, commentPutRequestDto))
                .build();
    }

    @DeleteMapping("/api/board/comment/{commentId}")
    public ResponseDto<CommentDeleteResponseDto> deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                  @PathVariable Long commentId) {
        return ResponseDto.<CommentDeleteResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("댓글 삭제")
                .data(commentService.deleteComment(userDetails, commentId))
                .build();
    }
}
