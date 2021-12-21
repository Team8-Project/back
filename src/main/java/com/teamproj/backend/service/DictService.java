package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import com.teamproj.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictService {
    private final String NULL_DICT_MSG = "존재하지 않는 사전입니다.";
    private final DictRepository dictRepository;
    private final DictHistoryRepository dictHistoryRepository;

    // 사전 목록 가져오기
    public List<DictResponseDto> getDicts(int page, int size) {
        Page<Dict> dict = dictRepository.findAll(PageRequest.of(page, size));
        if (dict.hasContent()){
            return dictListToDictResponseDtoList(dict.getContent());
        }else{
            return new ArrayList<>();
        }
    }

    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Dict> dictList) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();
        for(Dict dict : dictList){
            dictResponseDtoList.add(DictResponseDto.builder()
                    // .element(dict.beeeeee())
                    .build());
        }
        return dictResponseDtoList;
    }

    // 사전 상세 정보 가져오기
    public DictDetailResponseDto getDictDetail(Long dictId){
        Optional<Dict> dict = dictRepository.findById(dictId);
        nullCheck(dict, NULL_DICT_MSG);

        // 사전에 들어갈 내용이 정해지면 작성됩니다.
        DictDetailResponseDto dictDetailResponseDto = null;

        return dictDetailResponseDto;
    }

    // 사전 작성하기
    public DictPostResponseDto postDict(UserDetailsImpl userDetails, DictPostRequestDto dictPostRequestDto){
        loginCheck(userDetails);

        if(dictRepository.existsByDictName(dictPostRequestDto.getDictName())){
            throw new IllegalArgumentException("이미 존재하는 글입니다.");
        }

        Dict dict = Dict.builder()
                // TODO
                .user(userDetails.getUser())
                .content(dictPostRequestDto.getContent())
                .dictName(dictPostRequestDto.getDictName())
                .build();
        // 이후 작업은 API 명세표 정해지고 시작합니다.
        return DictPostResponseDto.builder()
                .result("작성 성공")
                .build();
    }

    // 사전 수정하기 및 수정 내역에 저장
    @Transactional
    public DictPutResponseDto putDict(UserDetailsImpl userDetails, Long dictId, DictPutRequestDto dictPutRequestDto){
        loginCheck(userDetails);

        Optional<Dict> dict = dictRepository.findById(dictId);
        nullCheck(dict, NULL_DICT_MSG);

        DictHistory dictHistory = DictHistory.builder()
                .prevContent(dict.get().getContent())
                .user(dict.get().getUser())
                .dict(dict.get())
                .build();

        // 이전 내용 히스토리에 저장
        dictHistoryRepository.save(dictHistory);

        dict.get().setContent(dictPutRequestDto.getContent());
        
        // 변화 내용 사전에 저장
        dictRepository.save(dict.get());

        return DictPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    public void nullCheck(Optional<Dict> optional, String msg){
        if(!optional.isPresent()){
            throw new NullPointerException(msg);
        }
    }
    public void loginCheck(UserDetailsImpl userDetails){
        if(userDetails == null){
            throw new NullPointerException("로그인하지 않은 사용자입니다.");
        }
    }
}
