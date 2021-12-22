package com.teamproj.backend.controller;

import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/api/quiz")
    public ResponseEntity<List<QuizResponseDto>> getQuizList(@RequestParam int count){
        return ResponseEntity.ok()
                .body(quizService.getQuizList(count));
    }
}
