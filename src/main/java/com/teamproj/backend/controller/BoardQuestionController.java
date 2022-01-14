package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.board.BoardQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BoardQuestionController {

    private final BoardQuestionService boardQuestionService;

    @GetMapping("/api/board/question/select/{commentId}")
    public ResponseDto<String> selectAnswer(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable Long commentId) {

        return ResponseDto.<String>builder()
                .status(HttpStatus.OK.toString())
                .message("success")
                .data(boardQuestionService.selectAnswer(userDetails, commentId))
                .build();
    }
}
