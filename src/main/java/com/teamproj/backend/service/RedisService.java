package com.teamproj.backend.service;

import com.teamproj.backend.dto.alarm.AlarmResponseDto;
import com.teamproj.backend.dto.board.BoardMemeBest.BoardMemeBestResponseDto;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.dto.quiz.QuizResponseDto;
import com.teamproj.backend.dto.statistics.StatDictResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

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
    private final RedisTemplate<String, StatDictResponseDto> redisStatDictResponseDtoTemplate;
    private final RedisTemplate<String, AlarmResponseDto> redisAlarmResponseDtoTemplate;

    public void setAlarm(String key, List<AlarmResponseDto> object){
        redisTemplate.delete(key);
        ListOperations<String, AlarmResponseDto> redis = redisAlarmResponseDtoTemplate.opsForList();
        redis.leftPushAll(key, object);
    }
    public List<AlarmResponseDto> getAlarm(String key) {
        ListOperations<String, AlarmResponseDto> list = redisAlarmResponseDtoTemplate.opsForList();

        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    public void setStatDict(String key, StatDictResponseDto object) {
        redisTemplate.delete(key);
        ValueOperations<String, Object> redis = redisTemplate.opsForValue();
        redis.set(key, object);
        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    public StatDictResponseDto getStatDict(String key) {
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(StatDictResponseDto.class));
        ValueOperations<String, StatDictResponseDto> redis = redisStatDictResponseDtoTemplate.opsForValue();
        StatDictResponseDto data = redis.get(key);
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return data;
    }

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

    public void setRandomQuiz(String key, List<QuizResponseDto> quizResponseDtoList) {
        redisTemplate.delete(key);
        ListOperations<String, QuizResponseDto> list = redisQuizResponseDtoTemplate.opsForList();
        list.leftPushAll(key, quizResponseDtoList);
        redisQuizResponseDtoTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    public List<QuizResponseDto> getRandomQuiz(String key) {
        ListOperations<String, QuizResponseDto> list = redisQuizResponseDtoTemplate.opsForList();
        if (list.size(key) > 0) {
            return list.range(key, 0, list.size(key) - 1);
        }

        return null;
    }

    // Utils


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

    public String getDictHealth(String key) {
        ValueOperations<String, String> redis = redisStringTemplate.opsForValue();
        return redis.get(key);
    }

    public void setDictHealth(String key, String str) {
        ValueOperations<String, String> redis = redisStringTemplate.opsForValue();
        redis.set(key, str);
        redisStringTemplate.expire(key, 15, TimeUnit.SECONDS);
    }
    //endregion
}

