package com.teamproj.backend.controller;

import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/api/board/{postId}/comment")
    public ResponseEntity<CommentPostResponseDto> postComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                              @PathVariable Long postId,
                                                              @RequestBody CommentPostRequestDto commentPostRequestDto){
        return ResponseEntity.ok()
                .body(commentService.postComment(userDetails, postId, commentPostRequestDto));
    }

    @PutMapping("/api/board/comment/{commentId}")
    public ResponseEntity<CommentPutResponseDto> putComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @PathVariable Long commentId,
                                                            @RequestBody CommentPutRequestDto commentPutRequestDto){
        return ResponseEntity.ok()
                .body(commentService.putComment(userDetails, commentId, commentPutRequestDto));
    }

    @DeleteMapping("/api/board/comment/{commentId}")
    public ResponseEntity<CommentDeleteResponseDto> deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                  @PathVariable Long commentId){
        return ResponseEntity.ok()
                .body(commentService.deleteComment(userDetails, commentId));
    }
}
