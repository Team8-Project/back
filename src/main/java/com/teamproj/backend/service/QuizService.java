package com.teamproj.backend.service;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.model.quiz.*;
import com.teamproj.backend.util.MySqlJpaTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.teamproj.backend.util.RedisKey.*;
import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final EntityManager entityManager;
    private final StatService statService;
    private final RedisService redisService;

    // 문제 목록 불러오기
    public List<QuizResponseDto> getQuizList(int count, String category) {
        List<QuizResponseDto> quizResponseDtoList = getSafeQuizResponseDtoList(RANDOM_QUIZ_KEY, count, category);

        // 통계 구문. 쓰레드로 동작... 하게 할 예정;;
        statService.statQuizStarter(category);

        return quizResponseDtoList;
    }

    // region 보조 기능
    // Utils
    // 퀴즈 목록을 랜덤하게 count 개 받아오는 기능
    private List<Quiz> randomQuizPick(String category) {
        // count 개수 만큼의 레코드를 랜덤하게 받아오는 구문
        // MySqlJpaTemplates.DEFAULT : NumberExpression.random().asc()를 MySQL 에서 사용 가능하도록 튜닝한 템플릿.
        JPAQuery<Quiz> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
        QQuiz qQuiz = new QQuiz("quiz");
        QQuizBank qQuizBank = QQuizBank.quizBank;

        return query.from(qQuiz)
                .leftJoin(qQuiz.choiceList, qQuizBank)
                .fetchJoin()
                .where(qQuiz.category.eq(category))
                .orderBy(NumberExpression.random().asc())
                .fetch();
    }

    // QuizBankList to StringList
    private List<String> quizBankListToStringList(List<QuizBank> choiceList) {
        List<String> result = new ArrayList<>();

        for (QuizBank choice : choiceList) {
            result.add(choice.getChoice());
        }

        return result;
    }

    // Get SafeEntity
    // QuizResponseDtoList
    private List<QuizResponseDto> getSafeQuizResponseDtoList(String key, int count, String category) {
        List<QuizResponseDto> quizResponseDtoList = redisService.getRandomQuiz(key + category);

        if(quizResponseDtoList == null){
            // QueryDSL 적용 구문
            List<Quiz> quizList = randomQuizPick(category);
            // DtoList 로 반환하는 과정에서 문제 속의 선택지 순서도 섞임
            redisService.setRandomQuiz(key + category, quizListToQuizResponseDtoList(quizList));
            quizResponseDtoList = redisService.getRandomQuiz(key + category);

            if(quizResponseDtoList == null){
                throw new NullPointerException(NOT_EXIST_CATEGORY);
            }
        }

        Collections.shuffle(quizResponseDtoList);
        return quizResponseDtoList.subList(0, Math.min(count, quizResponseDtoList.size()));
    }

    // Entity to Dto
    // QuizList to QuizResponseDtoList
    private List<QuizResponseDto> quizListToQuizResponseDtoList(List<Quiz> quizList) {
        List<QuizResponseDto> quizResponseDtoList = new ArrayList<>();

        for (Quiz quiz : quizList) {
            List<String> choiceList = quizBankListToStringList(quiz.getChoiceList());
            Collections.shuffle(choiceList);

            quizResponseDtoList.add(QuizResponseDto.builder()
                    .question(quiz.getQuestion())
                    .solution(quiz.getSolution().getChoice())
                    .choice(choiceList)
                    .build());
        }

        return quizResponseDtoList;
    }
    // endregion
}
