package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.dto.main.MainPageResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.main.CarouselImage;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final CarouselImageRepository carouselImageRepository;
    private final DictService dictService;
    private final RedisTemplate<String, Object> redisTemplate;

    public MainPageResponseDto getMainPageElements(String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = userDetails == null ? null : jwtAuthenticateProcessor.getUser(userDetails);

        List<CarouselImage> carouselImageList = carouselImageRepository.findAll();
        List<MainTodayMemeResponseDto> todayList = dictService.getTodayMeme(20);

//        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
//        operations.set("test", "test");
//        String redis = (String)operations.get("test");
//        System.out.println(redis);

        return MainPageResponseDto.builder()
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getNickname())
                .carousels(carouselImageListToStringList(carouselImageList))
                .todayList(todayList)
                .build();
    }

    private List<String> carouselImageListToStringList(List<CarouselImage> carouselImageList) {
        List<String> result = new ArrayList<>();
        for (CarouselImage carouselImage : carouselImageList) {
            result.add(carouselImage.getImageUrl());
        }
        return result;
    }
}
