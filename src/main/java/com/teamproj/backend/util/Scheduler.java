package com.teamproj.backend.util;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.service.DictService;
import com.teamproj.backend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.teamproj.backend.util.RedisKey.*;

@Component
@RequiredArgsConstructor
public class Scheduler {
    private final CarouselImageRepository carouselImageRepository;
    private final DictService dictService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisService redisService;

    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 0 * * *")
    public void mainPageData() {
        System.out.println("메인 페이지 데이터 교체 .....");
        // 캐러셀이미지, 오늘의밈 제품 교체
        redisTemplate.delete(CAROUSEL_URL_KEY);
        redisTemplate.delete(TODAY_LIST_KEY);
        redisService.setCarouselImageUrl(CAROUSEL_URL_KEY, carouselImageRepository.findAll());
        redisService.setTodayList(TODAY_LIST_KEY, dictService.getTodayMeme(20));
    }
}
