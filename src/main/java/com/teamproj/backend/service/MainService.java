package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.dto.main.MainPageResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.teamproj.backend.util.RedisKey.CAROUSEL_URL_KEY;
import static com.teamproj.backend.util.RedisKey.TODAY_LIST_KEY;

@Service
@RequiredArgsConstructor
public class MainService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    private final DictService dictService;
    private final RedisService redisService;

    private final CarouselImageRepository carouselImageRepository;


    public MainPageResponseDto getMainPageElements(String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = userDetails == null ? null : jwtAuthenticateProcessor.getUser(userDetails);

        List<String> carouselImageUrlList = getSafeCarouselImageUrlList(CAROUSEL_URL_KEY);
        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = getSafeMainTodayMemeResponseDtoList(redisService.getTodayList(TODAY_LIST_KEY));

        return MainPageResponseDto.builder()
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getNickname())
                .carousels(carouselImageUrlList)
                .todayList(mainTodayMemeResponseDtoList)
                .build();
    }

    // get SafeEntity
    // CarouselImageUrlList
    private List<String> getSafeCarouselImageUrlList(String key) {
        List<String> carouselImageUrlList = redisService.getStringList(key);

        if (carouselImageUrlList == null) {
            redisService.setCarouselImageUrl(key, carouselImageRepository.findAll());
            carouselImageUrlList = redisService.getStringList(key);

            if (carouselImageUrlList == null) {
                return new ArrayList<>();
            }
        }

        return carouselImageUrlList;
    }

    // MainTodayMemeResponseDtoList
    private List<MainTodayMemeResponseDto> getSafeMainTodayMemeResponseDtoList(List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList) {
        if (mainTodayMemeResponseDtoList == null) {
            redisService.setTodayList(TODAY_LIST_KEY, dictService.getTodayMeme(20));
            mainTodayMemeResponseDtoList = redisService.getTodayList(TODAY_LIST_KEY);

            if (mainTodayMemeResponseDtoList == null) {
                return new ArrayList<>();
            }
        }
        // 섞은 다음 7개만 뽑아내기
        Collections.shuffle(mainTodayMemeResponseDtoList);
        int returnSize = Math.min(mainTodayMemeResponseDtoList.size(), 7);

        return mainTodayMemeResponseDtoList.subList(0, returnSize);
    }

}
