package com.teamproj.backend.service;

import com.teamproj.backend.Repository.stat.StatQuizSolverRepository;
import com.teamproj.backend.Repository.stat.StatQuizStarterRepository;
import com.teamproj.backend.Repository.stat.StatVisitorRepository;
import com.teamproj.backend.model.statistics.StatNumericData;
import com.teamproj.backend.model.statistics.StatQuizSolver;
import com.teamproj.backend.model.statistics.StatQuizStarter;
import com.teamproj.backend.model.statistics.StatVisitor;
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

    public long statVisitor() {
        StatVisitor statVisitor = getSaveStatVisitorByVisitorIp(StatisticsUtils.getClientIp(), StatisticsUtils.getClientReferer());
        statVisitorRepository.save(statVisitor);

        return statVisitorRepository.count();
    }

    public void statQuizStarter(String category){
        statQuizStarterRepository.save(StatQuizStarter.builder()
                .type(category)
                .starterIp(StatisticsUtils.getClientIp())
                .build());
    }

    public void statQuizSolver(String category, int score){
        statQuizSolverRepository.save(StatQuizSolver.builder()
                .type(category)
                .solverIp(StatisticsUtils.getClientIp())
                .score(score)
                .build());
    }

    @Transactional
    public void statVisitorToNumericData(Long statVisitorCnt, StatNumericData statNumericData){
        statNumericData.setData(statNumericData.getData() + statVisitorCnt);
        statVisitorRepository.deleteAll();
    }

    // GetSafeEntity
    // StatVisitor
    private StatVisitor getSaveStatVisitorByVisitorIp(String visitorIp, String referer) {
        Optional<StatVisitor> statVisitor = statVisitorRepository.findByVisitorIp(visitorIp);
        return statVisitor.orElseGet(() -> StatVisitor.builder()
                .visitorIp(visitorIp)
                .referer(referer)
                .build());
    }
}
