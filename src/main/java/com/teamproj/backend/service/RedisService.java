package com.teamproj.backend.service;

import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.main.CarouselImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisService {
    /*
    1. 메인페이지
    1. (보류대상작업) 기한 지난 캐러셀이미지 출력 안함
    2. 캐러셀 이미지 url
    3. 오늘의 밈
    4. 명예의 전당
    5. 인기 게시글
2. 게시글 검색
    1. 인기 검색어
3. 사전 검색
    1. 추천 검색어
4. 게시글 작성
    1. 추천 해시태그
5. (미정) 퀴즈
    1. 퀴즈 목록 고정하기로 결정할 시 고정
6. 이미지 관련
    1. (우선순위 낮음) 게시글 작성 시 본문에 포함되지 않은 이미지들 정기적으로 삭제
     */
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTemplate<String, MainTodayMemeResponseDto> redisMainTodayMemeResponseDtoTemplate;

    public void setCarouselImageUrl(String key, List<CarouselImage> carouselImageList) {
        ValueOperations<String, String> operations = redisStringTemplate.opsForValue();
        operations.set(key, "hello world!");
        operations.get(key);
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, carouselImageListToStringList(carouselImageList));
        list.range(key, 0, list.size(key)-1);
    }

    public List<String> getList(String key) {
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        return list.range(key, 0, list.size(key) - 1);
    }

    public void setTodayList(String key, List<MainTodayMemeResponseDto> todayList) {
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();
        list.leftPushAll(key, todayList);
    }

    public List<MainTodayMemeResponseDto> getTodayList(String key) {
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();
        return list.range(key, 0, list.size(key) - 1);
    }

    public List<String> carouselImageListToStringList(List<CarouselImage> carouselImageList) {
        List<String> result = new ArrayList<>();
        for (CarouselImage carouselImage : carouselImageList) {
            result.add(carouselImage.getImageUrl());
        }
        return result;
    }
}

