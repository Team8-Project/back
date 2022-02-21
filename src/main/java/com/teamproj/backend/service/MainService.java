package com.teamproj.backend.service;

import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainPageResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.dict.DictService;
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

    // 메인페이지 데이터 불러오기
    public MainPageResponseDto getMainPageElements(String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = userDetails == null ? null : jwtAuthenticateProcessor.getUser(userDetails);

        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = getSafeMainTodayMemeResponseDtoList(TODAY_LIST_KEY);
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = getSafeMainMemeImageResponseDtoList(TODAY_MEME_IMAGE_LIST_KEY);
        
        return MainPageResponseDto.builder()
                .username(user == null ? null : user.getUsername())
                .nickname(user == null ? null : user.getNickname())
                .todayMemes(mainTodayMemeResponseDtoList)
                .popularImages(mainMemeImageResponseDtoList)
                .build();
    }

    // get SafeEntity
    // MainTodayMemeResponseDtoList
    // 오늘의밈(사전) : 전날 조회수 상위 20개의 목록을 받아 섞은 뒤 7개만 반환 : 사용자는 오늘의 밈 데이터를 정해진 풀 속에서 랜덤하게 받아오는것으로 인지.
    private List<MainTodayMemeResponseDto> getSafeMainTodayMemeResponseDtoList(String key) {
        // 1. Redis 에서 데이터 조회 시도
        List<MainTodayMemeResponseDto> mainTodayMemeResponseDtoList = redisService.getTodayList(key);

        // 2-1. 조회 결과 사이즈 0일 경우 null 로 반환하게 설정해놨음 == null 은 빈 데이터라는 소리
        // 2-2. 조회 결과가 20개보다 작을 경우 충분한 랜덤성을 제공하기 어렵다고 판단, 20개가 될 때까지 계속 스캔을 진행하도록 설정.
        //      Q) 아니, 그러면 처음 20회 조회는 무조건 이 불필요한 과정을 거친다는 말인가요 ??!?!?!!
        //      A) 그건 아니다. 전날의 데이터를 기반으로 하기 때문에 전날 20종류 이상의 조회가 있었다면 진행하지 않는 구문이다.
        //         이건 사람들이 열람한 사전 데이터가 20종류가 넘지 않을 것을 고려해 넣은 안전장치이다.
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

        // getSafeMainTodayMemeResponseDtoList 기능과 동일함.
        if (mainMemeImageResponseDtoList == null || mainMemeImageResponseDtoList.size() < 5) {
            List<MainMemeImageResponseDto> setElement = boardService.getTodayImage(5);

            if (setElement.size() > 0) {
                redisService.setTodayMemeImageList(key, setElement);
                mainMemeImageResponseDtoList = redisService.getTodayMemeImageList(key); // get List
            } else {
                mainMemeImageResponseDtoList = boardService.getRecentImage(5);
            }
        }

        return mainMemeImageResponseDtoList;
    }
}
