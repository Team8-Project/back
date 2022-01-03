package com.teamproj.backend.service;

import com.teamproj.backend.dto.board.BoardMemeBest.BoardMemeBestResponseDto;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.model.board.BoardHashTag;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTemplate<String, MainTodayMemeResponseDto> redisMainTodayMemeResponseDtoTemplate;
    private final RedisTemplate<String, MainMemeImageResponseDto> redisMainMemeImageResponseDtoTemplate;
    private final RedisTemplate<String, MainTodayBoardResponseDto> redisMainTodayBoardResponseDtoTemplate;
    private final RedisTemplate<String, QuizResponseDto> redisQuizResponseDtoTemplate;
    private final RedisTemplate<String, BoardMemeBestResponseDto> redisMemeBestResponseDtoTemplate;

    public void setBestDict(String key, List<String> bestDictList) {
        redisTemplate.delete(key);
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, bestDictList);
    }

    public void setRecommendSearch(String key, List<String> recommendSearch) {
        redisTemplate.delete(key);
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, recommendSearch);
        redisStringTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public void setCarouselImageUrl(String key, List<CarouselImage> carouselImageList) {
        redisTemplate.delete(key);
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, carouselImageListToStringList(carouselImageList));
    }

    public List<String> getStringList(String key) {
        ListOperations<String, String> list = redisStringTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setTodayList(String key, List<MainTodayMemeResponseDto> todayList) {
        redisTemplate.delete(key);
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();
        list.leftPushAll(key, todayList);
    }


    public List<MainTodayMemeResponseDto> getTodayList(String key) {
        ListOperations<String, MainTodayMemeResponseDto> list = redisMainTodayMemeResponseDtoTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    //region 추천 해시태그
    public void setRecommendHashTag(String key, List<BoardHashTag> boardHashTagList) {
        redisTemplate.delete(key);
        ListOperations<String, String> list = redisStringTemplate.opsForList();
        list.leftPushAll(key, hashTagListToStringList(boardHashTagList));
        redisStringTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    public List<String> getRecommendHashTag(String key) {
        ListOperations<String, String> list = redisStringTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }
        return null;
    }
    //endregion

    public List<MainMemeImageResponseDto> getTodayMemeImageList(String key) {
        ListOperations<String, MainMemeImageResponseDto> list = redisMainMemeImageResponseDtoTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setTodayMemeImageList(String key, List<MainMemeImageResponseDto> mainMemeImageResponseDtoList) {
        redisTemplate.delete(key);
        ListOperations<String, MainMemeImageResponseDto> list = redisMainMemeImageResponseDtoTemplate.opsForList();
        list.leftPushAll(key, mainMemeImageResponseDtoList);
    }

    public List<MainTodayBoardResponseDto> getTodayBoardList(String key) {
        ListOperations<String, MainTodayBoardResponseDto> list = redisMainTodayBoardResponseDtoTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setTodayBoardList(String key, List<MainTodayBoardResponseDto> mainTodayBoardResponseDtoList) {
        redisTemplate.delete(key);
        ListOperations<String, MainTodayBoardResponseDto> list = redisMainTodayBoardResponseDtoTemplate.opsForList();
        list.leftPushAll(key, mainTodayBoardResponseDtoList);
    }

    public void setRandomQuiz(String key, List<QuizResponseDto> quizResponseDtoList){
        redisTemplate.delete(key);
        ListOperations<String, QuizResponseDto> list = redisQuizResponseDtoTemplate.opsForList();
        list.leftPushAll(key, quizResponseDtoList);
        redisQuizResponseDtoTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    public List<QuizResponseDto> getRandomQuiz(String key) {
        ListOperations<String, QuizResponseDto> list = redisQuizResponseDtoTemplate.opsForList();
        redisQuizResponseDtoTemplate.getExpire(key, TimeUnit.SECONDS);
        if(list.size(key) > 0){
            return list.range(key, 0, list.size(key) - 1);
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


    public List<String> hashTagListToStringList(List<BoardHashTag> boardHashTagList) {
        List<String> result = new ArrayList<>();
        for (BoardHashTag boardHashTag : boardHashTagList) {
            result.add(boardHashTag.getHashTagName());
        }

        return result;
    }

    //region 명예의 밈짤
    public List<BoardMemeBestResponseDto> getBestMemeImgList(String key) {
        ListOperations<String, BoardMemeBestResponseDto> list = redisMemeBestResponseDtoTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setBestMemeImgList(String key, List<BoardMemeBestResponseDto> boardMemeBestResponseDtoList) {
        redisTemplate.delete(key);
        ListOperations<String, BoardMemeBestResponseDto> list = redisMemeBestResponseDtoTemplate.opsForList();
        list.leftPushAll(key, boardMemeBestResponseDtoList);
    }
    //endregion
}

