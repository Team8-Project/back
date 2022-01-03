package com.teamproj.backend.util;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.Repository.board.BoardTodayLikeRepository;
import com.teamproj.backend.Repository.board.BoardViewersRepository;
import com.teamproj.backend.Repository.dict.DictViewersRepository;
import com.teamproj.backend.Repository.stat.StatNumericDataRepository;
import com.teamproj.backend.Repository.stat.StatVisitorRepository;
import com.teamproj.backend.service.BoardService;
import com.teamproj.backend.service.DictService;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.teamproj.backend.util.RedisKey.*;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final DictService dictService;
    private final StatService statService;
    private final BoardService boardService;

    private final CarouselImageRepository carouselImageRepository;
    private final StatVisitorRepository statVisitorRepository;
    private final StatNumericDataRepository statNumericdataRepository;
    private final BoardViewersRepository boardViewersRepository;
    private final DictViewersRepository dictViewersRepository;
    private final BoardTodayLikeRepository boardTodayLikeRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisService redisService;

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 * * *")
    public void dayRegularSchedule() {
        System.out.println("자정 정기 스케줄 실시 .....");
        // 캐러셀이미지, 오늘의밈, 인기게시글, 명예의전당 데이터 교체
        System.out.println("메인 페이지 데이터 교체 .....");
        redisService.setCarouselImageUrl(CAROUSEL_URL_KEY, carouselImageRepository.findAll());
        redisService.setTodayList(TODAY_LIST_KEY, dictService.getTodayMeme(20));
        redisService.setTodayMemeImageList(TODAY_MEME_IMAGE_LIST_KEY, boardService.getTodayImage(5));
        redisService.setTodayBoardList(TODAY_BOARD_LIST_KEY, boardService.getTodayBoard(5));
        boardTodayLikeRepository.resetAll();

        System.out.println("조회수 및 방문자 정보 초기화 .....");
        statService.statVisitorToNumericData(statVisitorRepository.count(), statNumericdataRepository.findByName("VISITOR"));
        boardViewersRepository.deleteAll();
        redisService.setBestDict(BEST_DICT_KEY, dictService.getSafeBestDict());
        dictViewersRepository.deleteAll();
    }

    @Scheduled(cron = "0 0 0 * * 0")
    public void weekendSchedule() {
        System.out.println("매주 일요일 스케줄 실시");
        // 명예의 밈짤 데이터 교체
        redisService.setBestMemeImgList(BEST_MEME_JJAL_KEY, boardService.getBestMemeImg("MEME"));
    }
}
