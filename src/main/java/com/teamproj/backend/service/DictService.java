package com.teamproj.backend.service;

import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import com.teamproj.backend.model.dict.DictLike;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.ManuallyJwtLoginProcessor;
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
    private final DictLikeRepository dictLikeRepository;
    private final ManuallyJwtLoginProcessor manuallyJwtLoginProcessor;

    // 사전 목록 가져오기
    public List<DictResponseDto> getDicts(int page, int size, String token) {
        UserDetailsImpl userDetails = manuallyJwtLoginProcessor.forceLogin(token);

        Page<Dict> dict = dictRepository.findAll(PageRequest.of(page, size));
        if (dict.hasContent()) {
            return dictListToDictResponseDtoList(dict.getContent(), userDetails);
        } else {
            return new ArrayList<>();
        }
    }

    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Dict> dictList, UserDetailsImpl userDetails) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();

        for (Dict dict : dictList) {
            dictResponseDtoList.add(DictResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .meaning(dict.getContent())
                    .isLike(isDictLike(dict, userDetails))
                    .likeCount(dict.getDictLikeList().size())
                    .build());
        }

        return dictResponseDtoList;
    }

    private boolean isDictLike(Dict dict, UserDetailsImpl userDetails) {
        // 1. 로그인하지 않았으면 무조건 false.
        // 2. dictLikeList가 비어있으면 무조건 false.
        // 3. 사용자의 dictLike 목록에 해당 dict가 포함되어있지 않으면 false.
        // 4. 포함되어있을시 true.
        if (userDetails == null) {
            return false;
        }
        Optional<DictLike> found = dictLikeRepository.findByUserAndDict(userDetails.getUser(), dict);
        return found.isPresent();
    }

    // 사전 상세 정보 가져오기
    public DictDetailResponseDto getDictDetail(Long dictId, String token) {
        UserDetailsImpl userDetails = manuallyJwtLoginProcessor.forceLogin(token);
        Dict dict = getDictSafe(dictId, NULL_DICT_MSG);

        return DictDetailResponseDto.builder()
                .dictId(dict.getDictId())
                .title(dict.getDictName())
                .meaning(dict.getContent())
                .firstWriter(dict.getFirstAuthor().getNickname())
                .recentWriter(dict.getRecentModifier().getNickname())
                .isLike(isDictLike(dict, userDetails))
                .createdAt(dict.getCreatedAt().toLocalDate())
                .modifiedAt(dict.getModifiedAt().toLocalDate())
                .build();
    }

    // 사전 작성하기
    public DictPostResponseDto postDict(UserDetailsImpl userDetails, DictPostRequestDto dictPostRequestDto) {
        loginCheck(userDetails);

        if (dictRepository.existsByDictName(dictPostRequestDto.getTitle())) {
            throw new IllegalArgumentException("이미 존재하는 글입니다.");
        }

        Dict dict = Dict.builder()
                .firstAuthor(userDetails.getUser())
                .recentModifier(userDetails.getUser())
                .content(dictPostRequestDto.getContent())
                .dictName(dictPostRequestDto.getTitle())
                .build();

        dictRepository.save(dict);

        return DictPostResponseDto.builder()
                .result("작성 성공")
                .build();
    }

    // 사전 수정하기 및 수정 내역에 저장
    @Transactional
    public DictPutResponseDto putDict(UserDetailsImpl userDetails, Long dictId, DictPutRequestDto dictPutRequestDto) {
        loginCheck(userDetails);

        Dict dict = getDictSafe(dictId, NULL_DICT_MSG);

        DictHistory dictHistory = DictHistory.builder()
                .prevContent(dict.getContent())
                .user(dict.getRecentModifier())
                .dict(dict)
                .build();

        // 이전 내용 히스토리에 저장
        dictHistoryRepository.save(dictHistory);

        dict.setContent(dictPutRequestDto.getContent());

        // 변화 내용 사전에 저장
        dictRepository.save(dict);

        return DictPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    public DictLikeResponseDto likeDict(UserDetailsImpl userDetails, Long dictId) {
        loginCheck(userDetails);

        Dict dict = getDictSafe(dictId, NULL_DICT_MSG);
        boolean isLike = false;
        if(isDictLike(dict, userDetails)){
            Optional<DictLike> dictLike = dictLikeRepository.findByUserAndDict(userDetails.getUser(), dict);
            if(!dictLike.isPresent()){
                throw new NullPointerException("존재하지 않는 좋아요..?");
            }
            dictLikeRepository.deleteById(dictLike.get().getDictLikeId());
        }else{
            DictLike dictLike = DictLike.builder()
                    .dict(dict)
                    .user(userDetails.getUser())
                    .build();
            dictLikeRepository.save(dictLike);
            isLike = true;
        }
        return DictLikeResponseDto.builder()
                .result(isLike)
                .build();
    }

    public Dict getDictSafe(Long dictId, String msg) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        if (!dict.isPresent()) {
            throw new NullPointerException(msg);
        }

        return dict.get();
    }

    public void loginCheck(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new NullPointerException("로그인하지 않은 사용자입니다.");
        }
    }


}
