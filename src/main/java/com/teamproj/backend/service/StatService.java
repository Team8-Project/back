package com.teamproj.backend.service;

import com.teamproj.backend.Repository.stat.StatBoardModifyRepository;
import com.teamproj.backend.Repository.stat.StatQuizSolverRepository;
import com.teamproj.backend.Repository.stat.StatQuizStarterRepository;
import com.teamproj.backend.Repository.stat.StatVisitorRepository;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.statistics.*;
import com.teamproj.backend.util.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatVisitorRepository statVisitorRepository;
    private final StatQuizStarterRepository statQuizStarterRepository;
    private final StatQuizSolverRepository statQuizSolverRepository;
    private final StatBoardModifyRepository statBoardModifyRepository;

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
    public void statVisitorToNumericData(Long statVisitorCnt, StatNumericData statNumericData){
        statNumericData.setData(statNumericData.getData() + statVisitorCnt);
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
