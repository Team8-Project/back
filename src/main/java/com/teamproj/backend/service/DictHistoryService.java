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

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictHistoryService {
    private final DictRepository dictRepository;
    private final DictHistoryRepository dictHistoryRepository;

    // 용어 사전 수정내역 목록
    public DictHistoryResponseDto getDictHistory(Long dictId) {
        Dict dict = getSafeDict(dictId);
        List<DictHistory> dictHistoryList = getSafeDictHistoryList(dict);

        return DictHistoryResponseDto.builder()
                .dictId(dict.getDictId())
                .title(dict.getDictName())
                .firstWriter(dict.getFirstAuthor().getNickname())
                .history(dictHistoryListToDictHistoryRecentResponseDtoList(dictHistoryList))
                .build();
    }

    public DictHistoryDetailResponseDto getDictHistoryDetail(Long historyId) {
        DictHistory dictHistory = getSafeDictHistory(historyId);

        return DictHistoryDetailResponseDto.builder()
                .dictId(dictHistory.getDict().getDictId())
                .revertFrom(dictHistory.getRevertFrom() == null ? null : dictHistory.getRevertFrom().getDictHistoryId())
                .title(dictHistory.getDict().getDictName())
                .firstWriter(dictHistory.getDict().getFirstAuthor().getNickname())
                .modifier(dictHistory.getUser().getNickname())
                .content(dictHistory.getPrevContent())
                .createdAt(dictHistory.getCreatedAt().toLocalDate())
                .build();
    }

    @Transactional
    public DictRevertResponseDto revertDict(Long historyId) {
        DictHistory dictHistory = getSafeDictHistory(historyId);
        Dict dict = getSafeDict(dictHistory.getDict().getDictId());

        DictHistory recentDict = DictHistory.builder()
                .prevContent(dict.getContent())
                .user(dict.getRecentModifier())
                .dict(dict)
                .revertFrom(dictHistory)
                .build();
        dictHistoryRepository.save(recentDict);

        dict.setContent(dictHistory.getPrevContent());
        dict.setRecentModifier(dictHistory.getUser());

        return DictRevertResponseDto.builder()
                .result("롤백 성공")
                .build();
    }


    // region 보조 기능
    // Get SafeEntity
    // Dict
    private Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        if (!dict.isPresent()) {
            throw new NullPointerException(NOT_EXIST_DICT);
        }
        return dict.get();
    }

    // DictHistory
    private DictHistory getSafeDictHistory(Long historyId) {
        Optional<DictHistory> dictHistory = dictHistoryRepository.findById(historyId);
        if (!dictHistory.isPresent()) {
            throw new NullPointerException(NOT_EXIST_DICT_HISTORY);
        }
        return dictHistory.get();
    }

    // DictHistoryList
    private List<DictHistory> getSafeDictHistoryList(Dict dict) {
        Optional<List<DictHistory>> dictHistory = dictHistoryRepository.findAllByDict(dict);
        return dictHistory.orElseGet(ArrayList::new);
    }

    // Entity To Dto
    // DictHistoryList to DictHistoryRecentResponseDtoList
    public List<DictHistoryRecentResponseDto> dictHistoryListToDictHistoryRecentResponseDtoList(List<DictHistory> dictHistoryList) {
        List<DictHistoryRecentResponseDto> dictHistoryRecentResponseDtoList = new ArrayList<>();

        for (DictHistory dictHistory : dictHistoryList) {
            dictHistoryRecentResponseDtoList.add(DictHistoryRecentResponseDto.builder()
                    .historyId(dictHistory.getDictHistoryId())
                    .revertFrom(dictHistory.getRevertFrom() == null ? null : dictHistory.getRevertFrom().getDictHistoryId())
                    .writer(dictHistory.getUser().getNickname())
                    .createdAt(dictHistory.getCreatedAt().toLocalDate())
                    .build());
        }

        return dictHistoryRecentResponseDtoList;
    }
    // endregion
}