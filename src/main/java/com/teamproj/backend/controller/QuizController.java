package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/api/quiz/{category}")
    public ResponseDto<List<QuizResponseDto>> getQuizList(@RequestParam int count,
                                                          @PathVariable String category){
        return ResponseDto.<List<QuizResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("퀴즈 불러오기")
                .data(quizService.getQuizList(count, category.toUpperCase()))
                .build();
    }
}
