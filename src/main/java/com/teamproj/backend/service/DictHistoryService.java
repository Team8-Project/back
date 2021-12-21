package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictHistoryService {
    private final DictRepository dictRepository;
    private final DictHistoryRepository dictHistoryRepository;

    // 용어 사전 수정내역 목록
    public List<DictHistoryResponseDto> dictHistory(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        if (!dict.isPresent()) {
            throw new NullPointerException("존재하지 않는 사전입니다.");
        }
        Optional<List<DictHistory>> dictHistoryList = dictHistoryRepository.findAllByDict(dict.get());

        return dictHistoryList.map(this::dictHistoryListToDictHistoryResponseDtoList).orElseGet(ArrayList::new);
    }

    private List<DictHistoryResponseDto> dictHistoryListToDictHistoryResponseDtoList(List<DictHistory> dictHistoryList) {
        List<DictHistoryResponseDto> dictHistoryResponseDtoList = new ArrayList<>();
        for(DictHistory dictHistory : dictHistoryList){
            dictHistoryResponseDtoList.add(DictHistoryResponseDto.builder()
                    .dictName(dictHistory.getDict().getDictName())
                    .author(dictHistory.getUser().getNickname())
                    .prevContent(dictHistory.getPrevContent())
                    .build());
        }
        return dictHistoryResponseDtoList;
    }
}
