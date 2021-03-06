package com.teamproj.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.RedisConnectionFailureException;
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

import static com.teamproj.backend.util.RedisKey.STAT_DICT_KEY;

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

    // ?????? ?????? ?????? ??????
    public StatDictResponseDto statDict() {
        StatDictResponseDto statDictResponseDto;
        try{
            statDictResponseDto = redisService.getStatDict(STAT_DICT_KEY);

            if (statDictResponseDto == null) {
                redisService.setStatDict(STAT_DICT_KEY, getStatDict());
                statDictResponseDto = redisService.getStatDict(STAT_DICT_KEY);
            }
        }catch(RedisConnectionFailureException e){
            statDictResponseDto = getStatDict();
        }

        ObjectMapper mapper = new ObjectMapper();

        return mapper.convertValue(statDictResponseDto, new TypeReference<StatDictResponseDto>() {
        });
    }

    private StatDictResponseDto getStatDict() {
        // ??? ?????? ??????
        Long dictCountAll = dictRepository.count();
        // ?????? 7?????? ????????? ?????? ??????(??????)
        List<StatDictPostByDayDto> dictCountWeeks = getDictPostByDay(6);
        // ??? ?????? ??????
        Long questionCountAll = dictQuestionRepository.countByEnabled(true);
        // ????????? ?????? ??????
        Long completeQuestionCountAll = questionSelectRepository.count();
        // ????????? ?????? ??????
        List<StatDictQuestionListDto> completeQuestion = getCompleteQuestionList(3);
        // ????????? ???????????? ?????? ??????
        Long remainQuestionCountAll = questionCountAll - completeQuestionCountAll;
        // ????????? ???????????? ?????? ??????
        List<StatDictQuestionListDto> remainQuestion = getRemainQuestionList(3);
        // ?????? ?????? ????????? ?????? ??????
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

    // ????????? ?????? ??????
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

    // ?????? ????????? ??????
    // ?????? ????????? ???????????????.
    // ?????? ????????? ?????? ???????????? ???????????? ????????? ????????? ????????????.....
    public long statVisitor(String clientIp, String referer) {
        StatVisitor statVisitor = getSafeStatVisitorByVisitorIp(clientIp, referer);
        statVisitorRepository.save(statVisitor); // modifiedAt(?????? ????????????) ???????????? ??????

        return statVisitorRepository.count();
    }

    // ?????? ????????? ?????? ??????
    public long getStatQuizStarter() {
        return statQuizStarterRepository.count();
    }

    // ?????? ??????????????? ??????
    public void statQuizStarter(String category, String clientIp) {
        statQuizStarterRepository.save(StatQuizStarter.builder()
                .type(category)
                .starterIp(clientIp)
                .build());
    }

    // ?????? ??? ?????? ??????. ?????????.
    public void statQuizSolver(String category, int score, String clientIp) {
        statQuizSolverRepository.save(StatQuizSolver.builder()
                .type(category)
                .solverIp(clientIp)
                .score(score)
                .build());
    }

    // ?????? ???????????? ??????.
    // ????????? ??? ?????? ?????? ??????????????? ????????? ?????? ??????????????? ?????? ??????.
    @Transactional
    public void statVisitorToNumericData(Long statVisitorCnt, String data) {
        StatNumericData statNumericData = statNumericDataRepository.findByName(data);
        statNumericData.setData(statNumericData.getData() + statVisitorCnt);
        statNumericDataRepository.save(statNumericData);
        statVisitorRepository.deleteAll();
    }

    //region ????????????
    // Utils
    // ?????? ????????? ?????????
    private List<Long> getSelectList() {
        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;
        return queryFactory
                .select(qQuestionSelect.dictQuestion.questionId)
                .from(qQuestionSelect)
                .fetch();
    }

    // ?????? ????????? ?????? ??????
    private List<StatDictQuestionListDto> getCompleteQuestionList(int size) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        List<Long> selectList = getSelectList();
        List<Tuple> tupleList = queryFactory
                .select(qDictQuestion.questionId, qDictQuestion.questionName, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.questionId.in(selectList),
                        qDictQuestion.enabled.eq(true))
                .orderBy(qDictQuestion.questionId.desc())
                .limit(size)
                .fetch();

        return questionTupleToDto(tupleList);
    }

    // ?????? ??? ??? ?????? ??????
    private List<StatDictQuestionListDto> getRemainQuestionList(int size) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        List<Long> selectList = getSelectList();
        List<Tuple> tupleList = queryFactory
                .select(qDictQuestion.questionId, qDictQuestion.questionName, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.questionId.notIn(selectList),
                        qDictQuestion.enabled.eq(true))
                .orderBy(qDictQuestion.questionId.desc())
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

    // ?????? ?????? ?????? ??????
    private List<StatDictPostByDayDto> getDictPostByDay(int day) {
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.of(0, 0, 0)); // ????????? ??? 00:00:00

        List<StatDictPostByDayDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            result.add(StatDictPostByDayDto.builder()
                    .date(localDateTime.plusDays(i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                    .count(0L)
                    .build());
        }

        Optional<List<Dict>> weekDict = dictRepository.findAllByCreatedAtGreaterThanEqual(localDateTime);
        if (!weekDict.isPresent() || weekDict.get().size() == 0) {
            return result;
        }

        LocalDate date = weekDict.get().get(0).getCreatedAt().toLocalDate();

        long count = 0L;
        for (Dict dict : weekDict.get()) {
            LocalDate dictDate = dict.getCreatedAt().toLocalDate();
            System.out.println("dictDate.equals(date) : " + dictDate.equals(date));
            if (dictDate.equals(date)) {
                count++;
            } else {
                String dateName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                System.out.println(dateName);
                // i = 6??? ??????(??????)??? for??? ???????????? ???????????? ????????? 6?????? ??????.
                for (int i = 0; i < 6; i++) {
                    if (result.get(i).getDate().equals(dateName)) {
                        result.remove(i);
                        result.add(i, StatDictPostByDayDto.builder()
                                .date(dateName)
                                .count(count)
                                .build());
                        break;
                    }
                }

                // ?????? ???????????? ????????? ?????? ??????.
                count = 1L;
                date = dictDate;
            }
        }

        // ?????? ??? ??????.
        if (count > 1) {
            String displayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            for(int i = 0; i < result.size(); i++){
                if(displayName.equals(result.get(i).getDate())){
                    result.remove(i);
                    result.add(i, StatDictPostByDayDto.builder()
                            .date(displayName)
                            .count(count)
                            .build());
                    break;
                }
            }
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
