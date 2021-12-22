package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryRecentResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
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
    public DictHistoryResponseDto getDictHistory(Long dictId) {
        Dict dict = getSafeDict(dictId);

        Optional<List<DictHistory>> dictHistoryList = dictHistoryRepository.findAllByDict(dict);

        return DictHistoryResponseDto.builder()
                .dictId(dict.getDictId())
                .title(dict.getDictName())
                .firstWriter(dict.getFirstAuthor().getNickname())
                .history(dictHistoryListToDictHistoryRecentResponseDtoList(dictHistoryList.orElseGet(ArrayList::new)))
                .build();
    }

    public List<DictHistoryRecentResponseDto> dictHistoryListToDictHistoryRecentResponseDtoList(List<DictHistory> dictHistoryList) {
        List<DictHistoryRecentResponseDto> dictHistoryRecentResponseDtoList = new ArrayList<>();

        for (DictHistory dictHistory : dictHistoryList) {
            dictHistoryRecentResponseDtoList.add(DictHistoryRecentResponseDto.builder()
                            .historyId(dictHistory.getDictHistoryId())
                            .writer(dictHistory.getUser().getNickname())
                            .createdAt(dictHistory.getCreatedAt().toLocalDate())
                            .build());
        }

        return dictHistoryRecentResponseDtoList;
    }

    public DictHistoryDetailResponseDto getDictHistoryDetail(Long historyId) {
        DictHistory dictHistory = getSafeDictHistory(historyId);

        return DictHistoryDetailResponseDto.builder()
                .dictId(dictHistory.getDict().getDictId())
                .title(dictHistory.getDict().getDictName())
                .firstWriter(dictHistory.getDict().getFirstAuthor().getNickname())
                .modifier(dictHistory.getUser().getNickname())
                .content(dictHistory.getPrevContent())
                .createdAt(dictHistory.getCreatedAt().toLocalDate())
                .build();
    }

    private Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        if (!dict.isPresent()) {
            throw new NullPointerException("존재하지 않는 사전입니다.");
        }
        return dict.get();
    }

    private DictHistory getSafeDictHistory(Long historyId) {
        Optional<DictHistory> dictHistory = dictHistoryRepository.findById(historyId);
        if(!dictHistory.isPresent()){
            throw new NullPointerException("유효하지 않은 역사입니다.");
        }
        return dictHistory.get();
    }
}