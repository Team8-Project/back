package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CarouselImageRepository;
import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
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
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;


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
                .todayMemes(mainTodayMemeResponseDtoList)
                .popularBoards(mainTodayBoardResponseDtoList)
                .popularImages(mainMemeImageResponseDtoList)
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
    // 오늘의밈(사전)
    private List<MainTodayMemeResponseDto> getSafeMainTodayMemeResponseDtoList(String key) {
        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = redisService.getTodayList(key);

        if (mainTodayMemeResponseDtoList == null || mainTodayMemeResponseDtoList.size() < 20) {
            List<MainTodayMemeResponseDto> setElement = dictService.getTodayMeme(20);

            if (setElement.size() > 0) {
                redisService.setTodayList(key, setElement);
                mainTodayMemeResponseDtoList = redisService.getTodayList(key);
            } else {
                return new ArrayList<>();
            }
        }
        // 섞은 다음 7개만 뽑아내기
        Collections.shuffle(mainTodayMemeResponseDtoList);
        int returnSize = Math.min(mainTodayMemeResponseDtoList.size(), 7);

        return mainTodayMemeResponseDtoList.subList(0, returnSize);
    }

    // MainMemeImageResponseDtoList
    // 명예의전당(게시판)
    public List<MainMemeImageResponseDto> getSafeMainMemeImageResponseDtoList(String key) {
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = redisService.getTodayMemeImageList(key);

        if (mainMemeImageResponseDtoList == null || mainMemeImageResponseDtoList.size() < 5) {
            List<MainMemeImageResponseDto> setElement = boardService.getTodayImage(5);

            if (setElement.size() > 0) {
                redisService.setTodayMemeImageList(key, setElement);
                mainMemeImageResponseDtoList = redisService.getTodayMemeImageList(key); // get List
            } else {
                return new ArrayList<>();
            }
        }

        return mainMemeImageResponseDtoList;
    }

    // MainTodayBoardResponseDtoList
    // 인기게시글(게시판)
    private List<MainTodayBoardResponseDto> getSafeMainTodayBoardResponseDtoList(String key) {
        List<MainTodayBoardResponseDto> mainTodayBoardResponseDtoList = redisService.getTodayBoardList(key);

        if (mainTodayBoardResponseDtoList == null || mainTodayBoardResponseDtoList.size() < 5) {
            List<MainTodayBoardResponseDto> setElement = boardService.getTodayBoard(5);

            if (setElement.size() > 0) {
                redisService.setTodayBoardList(key, setElement);
                mainTodayBoardResponseDtoList = redisService.getTodayBoardList(key);
            } else {
                return new ArrayList<>();
            }
        }

        return mainTodayBoardResponseDtoList;
    }

}
