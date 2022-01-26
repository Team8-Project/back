package com.teamproj.backend.service;

import com.teamproj.backend.Repository.quiz.QuizBankRepository;
import com.teamproj.backend.Repository.quiz.QuizRepository;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.model.quiz.Quiz;
import com.teamproj.backend.model.quiz.QuizBank;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class QuizServiceTest {
    @Autowired
    QuizService quizService;
    @Autowired
    QuizRepository quizRepository;
    @Autowired
    QuizBankRepository quizBankRepository;

    @BeforeEach
    void setUp() {
        List<Quiz> quizList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Quiz quiz = Quiz.builder()
                    .question("test" + i)
                    .quizImage("")
                    .category("TEST")
                    .build();
            quiz.addChoice(QuizBank.builder()
                    .quiz(quiz)
                    .choice("test")
                    .build());

            quizList.add(quiz);
        }
        quizList = quizRepository.saveAll(quizList);

        for (Quiz quiz : quizList) {
            quiz.setSolution(quiz.getChoiceList().get(0));
            quizRepository.save(quiz);
        }
    }

    @Test
    @DisplayName("성공")
    void success() {
        // given

        // when
        List<QuizResponseDto> result = quizService.getQuizList(10, "TEST", "127.0.0.1");
        System.out.println(result.get(0).getChoice().size());

        // then
        assertEquals(10, result.size());
    }
}
