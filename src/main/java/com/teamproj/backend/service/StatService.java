package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.Repository.stat.*;
import com.teamproj.backend.dto.rank.RankResponseDto;
import com.teamproj.backend.dto.statistics.StatDictPostByDayDto;
import com.teamproj.backend.dto.statistics.StatDictQuestionListDto;
import com.teamproj.backend.dto.statistics.StatDictResponseDto;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.QDictQuestion;
import com.teamproj.backend.model.dict.question.QQuestionSelect;
import com.teamproj.backend.model.statistics.*;
import com.teamproj.backend.util.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.teamproj.backend.util.RedisKey.*;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatVisitorRepository statVisitorRepository;
    private final StatQuizStarterRepository statQuizStarterRepository;
    private final StatQuizSolverRepository statQuizSolverRepository;
    private final StatBoardModifyRepository statBoardModifyRepository;
    private final StatNumericDataRepository statNumericDataRepository;
    private final StatQuestionModifyRepository statQuestionModifyRepository;

    private final DictRepository dictRepository;
    private final DictQuestionRepository dictQuestionRepository;
    private final QuestionSelectRepository questionSelectRepository;

    private final RankService rankService;
    private final RedisService redisService;

    private final JPAQueryFactory queryFactory;

    // 사전 통계 내용 출력
    public StatDictResponseDto statDict() {
        StatDictResponseDto statDictResponseDto = redisService.getStatDict(STAT_DICT_KEY);

        if(statDictResponseDto == null){
            redisService.setStatDict(STAT_DICT_KEY, getStatDict());
            return redisService.getStatDict(STAT_DICT_KEY);
        }

        return statDictResponseDto;
    }

    private StatDictResponseDto getStatDict() {
        // 총 단어 개수
        Long dictCountAll = dictRepository.count();
        // 최근 7일간 등록된 단어 개수(일별)
        List<StatDictPostByDayDto> dictCountWeeks = getDictPostByDay(7);
        // 총 질문 개수
        Long questionCountAll = dictQuestionRepository.countByEnabled(true);
        // 해결된 질문 개수
        Long completeQuestionCountAll = questionSelectRepository.count();
        // 해결된 질문 목록
        List<StatDictQuestionListDto> completeQuestion = getCompleteQuestionList(3);
        // 답변을 기다리는 질문 개수
        Long remainQuestionCountAll = questionCountAll - completeQuestionCountAll;
        // 답변을 기다리는 질문 목록
        List<StatDictQuestionListDto> remainQuestion = getRemainQuestionList(3);
        // 단어 많이 등록한 유저 랭킹
        List<RankResponseDto> dictPostRank = rankService.getRank(100);

        return StatDictResponseDto.builder()
                .dictCountAll(dictCountAll)
                .dictCountWeeks(dictCountWeeks)
                .questionCountAll(questionCountAll)
                .completeQuestionCountAll(completeQuestionCountAll)
                .completeQuestionList(completeQuestion)
                .remainQuestionCountAll(remainQuestionCountAll)
                .remainQuestionList(remainQuestion)
                .dictPostRank(dictPostRank)
                .build();
    }

    // 게시글 수정 내역
    public void statBoardModify(Board board) {
        StatBoardModify statBoardModify = StatBoardModify.builder()
                .boardId(board.getBoardId())
                .build();
        statBoardModifyRepository.save(statBoardModify);
    }

    public void statQuestionModify(DictQuestion dictQuestion) {
        StatQuestionModify statQuestionModify = StatQuestionModify.builder()
                .questionId(dictQuestion.getQuestionId())
                .build();
        statQuestionModifyRepository.save(statQuestionModify);
    }

    // 일일 방문자 통계
    // 하루 지나면 삭제됩니다.
    // 원래 데이터 전부 쌓아놓고 싶었는데 용량과 속도를 고려하여.....
    public long statVisitor() {
        StatVisitor statVisitor = getSafeStatVisitorByVisitorIp(StatisticsUtils.getClientIp(), StatisticsUtils.getClientReferer());
        statVisitorRepository.save(statVisitor); // modifiedAt(최근 방문시간) 기록하기 위해

        return statVisitorRepository.count();
    }

    // 퀴즈 시작한 사람 숫자
    public long getStatQuizStarter() {
        return statQuizStarterRepository.count();
    }

    // 퀴즈 시작한사람 통계
    public void statQuizStarter(String category) {
        statQuizStarterRepository.save(StatQuizStarter.builder()
                .type(category)
                .starterIp(StatisticsUtils.getClientIp())
                .build());
    }

    // 퀴즈 푼 사람 통계. 점수도.
    public void statQuizSolver(String category, int score) {
        statQuizSolverRepository.save(StatQuizSolver.builder()
                .type(category)
                .solverIp(StatisticsUtils.getClientIp())
                .score(score)
                .build());
    }

    // 전체 방문자수 통계.
    // 하루에 한 번씩 일일 방문자수의 레코드 수를 반영하도록 되어 있음.
    @Transactional
    public void statVisitorToNumericData(Long statVisitorCnt, String data) {
        StatNumericData statNumericData = statNumericDataRepository.findByName(data);
        statNumericData.setData(statNumericData.getData() + statVisitorCnt);
        statNumericDataRepository.save(statNumericData);
        statVisitorRepository.deleteAll();
    }

    //region 보조기능
    // Utils
    // 채택 테이블 리스트
    private List<Long> getSelectList() {
        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;
        return queryFactory
                .select(qQuestionSelect.dictQuestion.questionId)
                .from(qQuestionSelect)
                .fetch();
    }

    // 채택 완료된 질문 목록
    private List<StatDictQuestionListDto> getCompleteQuestionList(int size) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        List<Long> selectList = getSelectList();
        List<Tuple> tupleList = queryFactory
                .select(qDictQuestion.questionId, qDictQuestion.questionName, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.questionId.in(selectList))
                .limit(size)
                .fetch();

        return questionTupleToDto(tupleList);
    }

    // 채택 안 된 질문 목록
    private List<StatDictQuestionListDto> getRemainQuestionList(int size) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        List<Long> selectList = getSelectList();
        List<Tuple> tupleList = queryFactory
                .select(qDictQuestion.questionId, qDictQuestion.questionName, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.questionId.notIn(selectList))
                .limit(size)
                .fetch();

        return questionTupleToDto(tupleList);
    }

    // questionTupleList To DtoList
    private List<StatDictQuestionListDto> questionTupleToDto(List<Tuple> tupleList) {
        List<StatDictQuestionListDto> statDictQuestionListDtoList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            statDictQuestionListDtoList.add(StatDictQuestionListDto.builder()
                    .questionId(tuple.get(0, Long.class))
                    .title(tuple.get(1, String.class))
                    .profileImage(tuple.get(2, String.class))
                    .build());
        }
        return statDictQuestionListDtoList;
    }

    // 일일 사전 생성 통계
    private List<StatDictPostByDayDto> getDictPostByDay(int day) {
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.of(0, 0, 0)); // 일주일 전 00:00:00
        Optional<List<Dict>> weekDict = dictRepository.findAllByCreatedAtGreaterThanEqual(localDateTime);
        if (!weekDict.isPresent()) {
            return new ArrayList<>();
        }

        LocalDate date = weekDict.get().get(0).getCreatedAt().toLocalDate();
        List<StatDictPostByDayDto> result = new ArrayList<>();
        long count = 0L;
        for (Dict dict : weekDict.get()) {
            LocalDate dictDate = dict.getCreatedAt().toLocalDate();
            if (dictDate.equals(date)) {
                count++;
            } else {
                result.add(StatDictPostByDayDto.builder()
                        .date(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                        .count(count)
                        .build());
                count = 1L;
                date = dictDate;
            }
        }
        if (count > 0) {
            result.add(StatDictPostByDayDto.builder()
                    .date(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                    .count(count)
                    .build());
        }
        return result;
    }
    // GetSafeEntity
    // StatVisitor
    private StatVisitor getSafeStatVisitorByVisitorIp(String visitorIp, String referer) {
        Optional<StatVisitor> statVisitor = statVisitorRepository.findByVisitorIp(visitorIp);
        return statVisitor.orElseGet(() -> StatVisitor.builder()
                .visitorIp(visitorIp)
                .referer(referer)
                .build());
    }
    //endregion
}
