package com.teamproj.backend.service.stat;

import com.teamproj.backend.Repository.stat.StatNumericDataRepository;
import com.teamproj.backend.Repository.stat.StatQuizSolverRepository;
import com.teamproj.backend.dto.statistics.StatDictResponseDto;
import com.teamproj.backend.model.statistics.StatNumericData;
import com.teamproj.backend.service.StatService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class StatServiceTest {
    @Autowired
    private StatService statService;
    @Autowired
    private StatQuizSolverRepository statQuizSolverRepository;
    @Autowired
    private StatNumericDataRepository statNumericDataRepository;

    @Nested
    @DisplayName("사전 통계 호출")
    class StatDict {
        @Test
        @DisplayName("성공")
        void success() {
            // given

            // when
            StatDictResponseDto result = statService.statDict();

            // then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("방문자 정보")
    class StatVisit {
        @Test
        @DisplayName("방문자수 호출")
        void success() {
            // given

            // when
            Long result = statService.statVisitor("127.0.0.1", "test");

            // then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("퀴즈 정보")
    class StatQuiz {
        @Test
        @DisplayName("퀴즈 시도한 사람 수 호출")
        void quizStarter() {
            // given

            // when
            Long result = statService.getStatQuizStarter();

            // then
            assertNotNull(result);
        }

        @Nested
        @DisplayName("퀴즈 시도")
        class QuizTry {
            @Test
            @DisplayName("퀴즈 시도 기록")
            void quizStart() {
                // given
                Long quizStarter = statService.getStatQuizStarter();

                // when
                statService.statQuizStarter("lv1", "127.0.0.1");
                Long result = statService.getStatQuizStarter();

                // then
                assertEquals(quizStarter + 1, result);
            }

            @Test
            @DisplayName("퀴즈 풀이 기록")
            void quizSolve() {
                // given
                Long quizSolver = statQuizSolverRepository.count();

                // when
                statService.statQuizSolver("lv1", 10, "127.0.0.1");
                Long result = statQuizSolverRepository.count();

                // then
                assertEquals(quizSolver + 1, result);
            }
        }
    }

    @Nested
    @DisplayName("일일 조회수 갱신")
    class GetStatNumericData {
        @Test
        @DisplayName("성공")
        void success() {
            // given
            StatNumericData statNumericData = statNumericDataRepository.findByName("VISITOR");
            Long visitorCnt = statNumericData.getData();

            // when
            statService.statVisitorToNumericData(10L, "VISITOR");

            // then
            StatNumericData resultData = statNumericDataRepository.findByName("VISITOR");
            Long result = resultData.getData();

            assertEquals(visitorCnt + 10, result);
        }
    }
}
