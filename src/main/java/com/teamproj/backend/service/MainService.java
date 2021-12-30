package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainPageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.teamproj.backend.util.RedisKey.*;

@Service
@RequiredArgsConstructor
public class MainService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    private final BoardService boardService;
    private final DictService dictService;
    private final RedisService redisService;

    private final CarouselImageRepository carouselImageRepository;


    public MainPageResponseDto getMainPageElements(String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = userDetails == null ? null : jwtAuthenticateProcessor.getUser(userDetails);

        List<String> carouselImageUrlList = getSafeCarouselImageUrlList(CAROUSEL_URL_KEY);
        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = getSafeMainTodayMemeResponseDtoList(TODAY_LIST_KEY);
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = getSafeMainMemeImageResponseDtoList(TODAY_MEME_IMAGE_LIST_KEY);
        List<MainTodayBoardResponseDto> mainTodayBoardResponseDtoList = getSafeMainTodayBoardResponseDtoList(TODAY_BOARD_LIST_KEY);

        return MainPageResponseDto.builder()
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getNickname())
                .carousels(carouselImageUrlList)
                .todayList(mainTodayMemeResponseDtoList)
                .build();
    }

    // get SafeEntity
    // MainMemeImageResponseDtoList
    public List<MainMemeImageResponseDto> getSafeMainMemeImageResponseDtoList(String key){
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = redisService.getTodayMemeImageList(key);

        if (mainMemeImageResponseDtoList == null) {
            redisService.setTodayMemeImageList(key, boardService.getTodayImage(5));
            mainMemeImageResponseDtoList = redisService.getTodayMemeImageList(key); // get List

            if (mainMemeImageResponseDtoList == null) {
                mainMemeImageResponseDtoList = null; // null
            }
        }

        return mainMemeImageResponseDtoList;
    }

    // MainTodayBoardResponseDtoList
    private List<MainTodayBoardResponseDto> getSafeMainTodayBoardResponseDtoList(String key) {
        List<MainTodayBoardResponseDto> mainTodayBoardResponseDtoList = redisService.getTodayBoardList(key);

        if (mainTodayBoardResponseDtoList == null) {
            redisService.setTodayBoardList(key, boardService.getTodayBoard(5));
            mainTodayBoardResponseDtoList = redisService.getTodayBoardList(key);

            if (mainTodayBoardResponseDtoList == null) {
                mainTodayBoardResponseDtoList = null;
            }
        }

        return mainTodayBoardResponseDtoList;
    }

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
    private List<MainTodayMemeResponseDto> getSafeMainTodayMemeResponseDtoList(String key) {
        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = redisService.getTodayList(key);

        if (mainTodayMemeResponseDtoList == null) {
            redisService.setTodayList(key, dictService.getTodayMeme(20));
            mainTodayMemeResponseDtoList = redisService.getTodayList(key);

            if (mainTodayMemeResponseDtoList == null) {
                mainTodayMemeResponseDtoList = dictService.getTodayMeme(20);
            }
        }
        // 섞은 다음 7개만 뽑아내기
        Collections.shuffle(mainTodayMemeResponseDtoList);
        int returnSize = Math.min(mainTodayMemeResponseDtoList.size(), 7);

        return mainTodayMemeResponseDtoList.subList(0, returnSize);
    }

}
