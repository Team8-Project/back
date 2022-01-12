package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.stat.*;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.statistics.*;
import com.teamproj.backend.util.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatVisitorRepository statVisitorRepository;
    private final StatQuizStarterRepository statQuizStarterRepository;
    private final StatQuizSolverRepository statQuizSolverRepository;
    private final StatBoardModifyRepository statBoardModifyRepository;
    private final StatNumericDataRepository statNumericDataRepository;

    private final DictRepository dictRepository;

    // 사전 통계 내용 출력
    public void statDict(){
        // 총 단어 개수
        Long dictAllCount = dictRepository.count();
        // 오늘 등록된 단어 개수
        LocalDateTime yesterdayTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0)); //어제 00:00:00
        Long dictTodayCount = dictRepository.countByCreatedAtGreaterThanEqual(yesterdayTime);
        // 총 질문 개수
//        Long questionAllCount =
        // 해결된 질문 개수
        // 답변을 기다리는 질문 개수
        // 단어 많이 등록한 유저 랭킹
        // 일별 등록 단어 개수 그래프
    }

    // 게시글 수정 내역
    public void statBoardModify(Board board){
        StatBoardModify statBoardModify = StatBoardModify.builder()
                .boardId(board.getBoardId())
                .build();
        statBoardModifyRepository.save(statBoardModify);
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
    public long getStatQuizStarter(){
        return statQuizStarterRepository.count();
    }

    // 퀴즈 시작한사람 통계
    public void statQuizStarter(String category){
        statQuizStarterRepository.save(StatQuizStarter.builder()
                .type(category)
                .starterIp(StatisticsUtils.getClientIp())
                .build());
    }

    // 퀴즈 푼 사람 통계. 점수도.
    public void statQuizSolver(String category, int score){
        statQuizSolverRepository.save(StatQuizSolver.builder()
                .type(category)
                .solverIp(StatisticsUtils.getClientIp())
                .score(score)
                .build());
    }

    // 전체 방문자수 통계.
    // 하루에 한 번씩 일일 방문자수의 레코드 수를 반영하도록 되어 있음.
    @Transactional
    public void statVisitorToNumericData(Long statVisitorCnt, String data){
        StatNumericData statNumericData = statNumericDataRepository.findByName(data);
        statNumericData.setData(statNumericData.getData() + statVisitorCnt);
        statNumericDataRepository.save(statNumericData);
        statVisitorRepository.deleteAll();
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
}
