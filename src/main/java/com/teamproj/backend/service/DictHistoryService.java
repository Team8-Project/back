package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryRecentResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.dto.dictHistory.DictRevertResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_DICT;
import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_DICT_HISTORY;

@Service
@RequiredArgsConstructor
public class DictHistoryService {
    private final DictRepository dictRepository;
    private final DictHistoryRepository dictHistoryRepository;

    // 용어 사전 수정내역 목록
    public DictHistoryResponseDto getDictHistory(Long dictId) {
        Dict dict = getSafeDict(dictId);
        List<DictHistory> dictHistoryList = getSafeDictHistoryList(dict);

        User firstWriter = dict.getFirstAuthor();
        return DictHistoryResponseDto.builder()
                .dictId(dict.getDictId())
                .title(dict.getDictName())
                .firstWriter(firstWriter.getNickname())
                .firstWriterProfileImage(firstWriter.getProfileImage())
                .firstCreatedAt(dict.getCreatedAt())
                .history(dictHistoryListToDictHistoryRecentResponseDtoList(dictHistoryList))
                .build();
    }

    // 용어 사전 수정내역 상세
    public DictHistoryDetailResponseDto getDictHistoryDetail(Long historyId) {
        DictHistory dictHistory = getSafeDictHistory(historyId);
        Dict dict = dictHistory.getDict();

        return DictHistoryDetailResponseDto.builder()
                .dictId(dictHistory.getDict().getDictId())
                .revertFrom(dictHistory.getRevertFrom() == null ? null : dictHistory.getRevertFrom().getDictHistoryId())
                .title(dict.getDictName())
                .firstWriter(dict.getFirstAuthor().getNickname())
                .firstWriterProfileImage(dict.getFirstAuthor().getProfileImage())
                .firstCreatedAt(dict.getCreatedAt())
                .modifier(dictHistory.getUser().getNickname())
                .modifierProfileImage(dictHistory.getUser().getProfileImage())
                .summary(dictHistory.getPrevSummary())
                .content(dictHistory.getPrevContent())
                .createdAt(dictHistory.getCreatedAt())
                .build();
    }

    // 용어사전 롤백
    @Transactional
    public DictRevertResponseDto revertDict(Long historyId, UserDetailsImpl userDetails) {
        /*
            롤백 기능 수행 절차
            1. 기존의 데이터 recentDict 를 DictHistory 로 선언해 저장
            2. 롤백하고자 하는 데이터를 dict 로 덮어쓰기
            3. good!

            개선사항
            1. 롤백 기록 어떻게 해야할 지.
         */
        ValidChecker.loginCheck(userDetails);

        DictHistory dictHistory = getSafeDictHistory(historyId);
        Dict dict = getSafeDict(dictHistory.getDict().getDictId());

        DictHistory recentDict = DictHistory.builder()
                .prevSummary(dict.getSummary())
                .prevContent(dict.getContent())
                .user(dict.getRecentModifier())
                .dict(dict)
                .revertFrom(dictHistory)
                .build();
        dictHistoryRepository.save(recentDict);

        dict.setSummary(dictHistory.getPrevSummary());
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
        Optional<List<DictHistory>> dictHistory = dictHistoryRepository.findAllByDictOrderByCreatedAtDesc(dict);
        return dictHistory.orElseGet(ArrayList::new);
    }

    // Entity To Dto
    // DictHistoryList to DictHistoryRecentResponseDtoList
    public List<DictHistoryRecentResponseDto> dictHistoryListToDictHistoryRecentResponseDtoList(List<DictHistory> dictHistoryList) {
        List<DictHistoryRecentResponseDto> dictHistoryRecentResponseDtoList = new ArrayList<>();

        for (DictHistory dictHistory : dictHistoryList) {
            User user = dictHistory.getUser();
            dictHistoryRecentResponseDtoList.add(DictHistoryRecentResponseDto.builder()
                    .historyId(dictHistory.getDictHistoryId())
                    .revertFrom(dictHistory.getRevertFrom() == null ? null : dictHistory.getRevertFrom().getDictHistoryId())
                    .writerProfileImage(user.getProfileImage())
                    .writer(user.getNickname())
                    .createdAt(dictHistory.getCreatedAt())
                    .build());
        }

        return dictHistoryRecentResponseDtoList;
    }
    // endregion
}