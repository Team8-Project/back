package com.teamproj.backend.service;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.model.quiz.QQuiz;
import com.teamproj.backend.model.quiz.Quiz;
import com.teamproj.backend.model.quiz.QuizBank;
import com.teamproj.backend.util.MySqlJpaTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final EntityManager entityManager;

    // 문제 목록 불러오기
    public List<QuizResponseDto> getQuizList(int count) {
        // QueryDSL 적용 구문
        List<Quiz> quizList = randomQuizPick(count);

        // DtoList로 반환하는 과정에서 문제 속의 선택지 순서도 섞임
        return quizListToQuizResponseDtoList(quizList);
    }

    private List<Quiz> randomQuizPick(int count) {
        // count 개수 만큼의 레코드를 랜덤하게 받아오는 구문
        // MySqlJpaTemplates.DEFAULT : NumberExpression.random().asc()를 MySQL에서 사용 가능하도록 튜닝한 템플릿.
        JPAQuery<Quiz> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
        QQuiz qQuiz = new QQuiz("quiz");

        List<Quiz> quizList = query.from(qQuiz)
                .orderBy(NumberExpression.random().asc())
                .limit(count)
                .fetch();

        return quizList;
    }

    private List<QuizResponseDto> quizListToQuizResponseDtoList(List<Quiz> quizList) {
        List<QuizResponseDto> quizResponseDtoList = new ArrayList<>();

        for (Quiz quiz : quizList) {
            List<String> choiceList = getChoiceList(quiz.getChoiceList());
            Collections.shuffle(choiceList);

            quizResponseDtoList.add(QuizResponseDto.builder()
                    .question(quiz.getQuestion())
                    .solution(quiz.getSolution().getChoice())
                    .choice(choiceList)
                    .build());
        }

        return quizResponseDtoList;
    }

    private List<QuizResponseDto> quizPageToQuizResponseDtoList(Page<Quiz> quizPage) {
        List<QuizResponseDto> quizResponseDtoList = new ArrayList<>();
        if (quizPage.hasContent()) {
            for (Quiz quiz : quizPage.toList()) {
                List<String> choiceList = getChoiceList(quiz.getChoiceList());
                Collections.shuffle(choiceList);

                quizResponseDtoList.add(QuizResponseDto.builder()
                                .question(quiz.getQuestion())
                                .solution(quiz.getSolution().getChoice())
                                .choice(choiceList)
                                .build());
            }
        }
        return quizResponseDtoList;
    }

    private List<String> getChoiceList(List<QuizBank> choiceList) {
        List<String> result = new ArrayList<>();
        for(QuizBank choice : choiceList){
            result.add(choice.getChoice());
        }
        return result;
    }
}