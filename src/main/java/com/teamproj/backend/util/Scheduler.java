package com.teamproj.backend.util;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.Repository.board.BoardViewersRepository;
import com.teamproj.backend.Repository.stat.StatNumericDataRepository;
import com.teamproj.backend.Repository.stat.StatVisitorRepository;
import com.teamproj.backend.service.DictService;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.teamproj.backend.util.RedisKey.*;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final DictService dictService;
    private final StatService statService;

    private final CarouselImageRepository carouselImageRepository;
    private final StatVisitorRepository statVisitorRepository;
    private final StatNumericDataRepository statNumericdataRepository;
    private final BoardViewersRepository boardViewersRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisService redisService;

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 * * *")
    public void dayRegularSchedule() {
        System.out.println("자정 정기 스케줄 실시 .....");
        // 캐러셀이미지, 오늘의밈 데이터 교체
        System.out.println("메인 페이지 데이터 교체 .....");
        redisTemplate.delete(CAROUSEL_URL_KEY);
        redisTemplate.delete(TODAY_LIST_KEY);
        redisService.setCarouselImageUrl(CAROUSEL_URL_KEY, carouselImageRepository.findAll());
        redisService.setTodayList(TODAY_LIST_KEY, dictService.getTodayMeme(20));
        System.out.println("해시 태그 데이터 교체");
        redisTemplate.delete(HASHTAG_RECOMMEND_KEY);

        System.out.println("조회수 및 방문자 정보 초기화 .....");
        statService.statVisitorToNumericData(statVisitorRepository.count(), statNumericdataRepository.findByName("VISITOR"));
        boardViewersRepository.deleteAll();
    }

    // 현재는 매 시간 작업하도록 설정
//    @Scheduled(cron = "0 0 0/1 * * *")
//    public void refreshSchedule(){
//        System.out.println("비정기 작업 새로고침 실시 .....");
//    }
}
