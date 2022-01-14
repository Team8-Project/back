package com.teamproj.backend.service.dict;

import com.teamproj.backend.Repository.dict.DictCuriousRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dictCurious.DictCuriousPostRequestDto;
import com.teamproj.backend.dto.dictCurious.DictCuriousPostResponseDto;
import com.teamproj.backend.dto.dictCurious.DictCuriousResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.DictCurious;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictCuriousService {
    private final DictRepository dictRepository;
    private final DictCuriousRepository dictCuriousRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    // 요청 목록 조회
    public List<DictCuriousResponseDto> getDictCurious() {
        List<DictCurious> dictCuriousList = getSafeDictCuriousList();
        return dictCuriousListToDictCuriousResponseDtoList(dictCuriousList);
    }

    // 요청 작성
    public DictCuriousPostResponseDto postDictCurious(UserDetailsImpl userDetails, DictCuriousPostRequestDto dictCuriousPostRequestDto) {
        ValidChecker.loginCheck(userDetails);

        String curiousName = dictCuriousPostRequestDto.getCuriousName();
        // 1. 이미 사전에 등록되어있는 단어는 요청할 수 없음
        if (dictRepository.existsByDictName(curiousName)) {
            throw new IllegalArgumentException(EXIST_DICT);
        }

        // 사전에 존재하지 않으면 DB를 방문할 필요 없으므로 유저정보 가져오는 것은 검증 이후.
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        // 2. 같은 사람이 같은 요청을 두 번 보낼 수 없음
        if (dictCuriousRepository.existsByUserAndCuriousName(user, curiousName)) {
            throw new IllegalArgumentException(NOT_DUPLICATION_YOUR_REQUEST);
        }

        DictCurious dictCurious = dictCuriousRepository.save(DictCurious.builder()
                .curiousName(curiousName)
                .user(user)
                .build());

        return DictCuriousPostResponseDto.builder()
                .curiousId(dictCurious.getDictCuriousId())
                .curiousName(dictCurious.getCuriousName())
                .build();
    }

    // 요청 삭제(사용자가 직접)
    public String deleteDictCurious(UserDetailsImpl userDetails, Long dictCuriousId) {
        ValidChecker.loginCheck(userDetails);

        DictCurious dictCurious = getSafeDictCurious(dictCuriousId);
        if (!userDetails.getUsername().equals(dictCurious.getUser().getUsername())) {
            throw new IllegalArgumentException(NOT_YOUR_CURIOUS);
        }
        dictCuriousRepository.deleteById(dictCurious.getDictCuriousId());

        return "삭제 완료";
    }

    // 요청 해결 시 삭제
    public String completeDictCurious(List<Long> dictCuriousIdList){
        dictCuriousRepository.deleteAllById(dictCuriousIdList);

        return "삭제 완료";
    }

    private DictCurious getSafeDictCurious(Long dictCuriousId) {
        Optional<DictCurious> dictCurious = dictCuriousRepository.findById(dictCuriousId);
        return dictCurious.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT_CURIOUS));
    }

    private List<DictCurious> getSafeDictCuriousList() {
        // 나중에 옵션 붙을수도 있어서 별도 메소드로 분리해놓음.
        List<DictCurious> dictCuriousList = dictCuriousRepository.findAll();
        return dictCuriousList;
    }

    // Entity To Dto
    // DictCuriousList To DictCuriousResponseDtoList
    private List<DictCuriousResponseDto> dictCuriousListToDictCuriousResponseDtoList(List<DictCurious> dictCuriousList) {
        List<DictCuriousResponseDto> dictCuriousResponseDtoList = new ArrayList<>();

        // 1. curiousName 이 이미 존재하는지 찾아본다.
        // 2. 존재할 경우 curiousIdList 에 추가해준다.
        // 3. 존재하지 않을 경우 새로 추가해준다.
        List<String> usingNameList = new ArrayList<>();
        for (DictCurious dictCurious : dictCuriousList) {
            Long curiousId = dictCurious.getDictCuriousId();
            String curiousName = dictCurious.getCuriousName();

            // 이미 추가된 단어일 경우 교체
            if (usingNameList.contains(curiousName)) {
                for (int i = 0; i < dictCuriousResponseDtoList.size(); i++) {
                    DictCuriousResponseDto dto = dictCuriousResponseDtoList.get(i);
                    if (dto.getCuriousName().equals(curiousName)) {
                        List<Long> newCuriousIdList = dto.getCuriousId();
                        newCuriousIdList.add(curiousId);
                        dictCuriousResponseDtoList.set(i, DictCuriousResponseDto.builder()
                                .curiousId(newCuriousIdList)
                                .firstRequester(dto.getFirstRequester())
                                .curiousName(curiousName)
                                .build());

                        break;
                    }
                }
            } else {
                // 아직 추가되지 않은 단어일 경우 추가
                List<Long> curiousIdList = new ArrayList<>();
                curiousIdList.add(curiousId);
                dictCuriousResponseDtoList.add(DictCuriousResponseDto.builder()
                        .curiousId(curiousIdList)
                        .firstRequester(dictCurious.getUser().getNickname())
                        .curiousName(curiousName)
                        .build());
                
                // 추가된 단어 목록에 추가
                usingNameList.add(curiousName);
            }
        }

        return dictCuriousResponseDtoList;
    }
}
