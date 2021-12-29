package com.teamproj.backend.service;

import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.main.CarouselImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    /*
        1. 메인페이지
            - 명예의 전당
            - 인기 게시글
        2. 게시글 검색
            - 인기 검색어
        4. 게시글 작성
            - 추천 해시태그
        5. (미정) 퀴즈
            - 퀴즈 목록 고정하기로 결정할 시 고정
        6. 이미지 관련
            - (우선순위 낮음) 게시글 작성 시 본문에 포함되지 않은 이미지들 정기적으로 삭제
     */
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTemplate<String, MainTodayMemeResponseDto> redisMainTodayMemeResponseDtoTemplate;

    public void setRecommendSearch(String key, List<String> recommendSearch){
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, recommendSearch);
        redisStringTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public void setCarouselImageUrl(String key, List<CarouselImage> carouselImageList) {
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, carouselImageListToStringList(carouselImageList));
    }

    public List<String> getStringList(String key) {
        ListOperations<String, String> list = redisStringTemplate.opsForList();

        if(list.size(key)>0){
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setTodayList(String key, List<MainTodayMemeResponseDto> todayList) {
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();
        list.leftPushAll(key, todayList);
    }

    public List<MainTodayMemeResponseDto> getTodayList(String key) {
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();

        if(list.size(key)>0){
            list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    // Utils
    // CarouselImageList to StringList
    public List<String> carouselImageListToStringList(List<CarouselImage> carouselImageList) {
        List<String> result = new ArrayList<>();

        for (CarouselImage carouselImage : carouselImageList) {
            result.add(carouselImage.getImageUrl());
        }

        return result;
    }
}

