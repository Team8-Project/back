package com.teamproj.backend.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictRepository dictRepository;
    private final DictHistoryRepository dictHistoryRepository;
    private final DictLikeRepository dictLikeRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    // 사전 목록 가져오기
    public List<DictResponseDto> getDictList(int page, int size, String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        List<Dict> dictList = getSafeDictPage(page, size);
        return dictListToDictResponseDtoList(dictList, userDetails);
    }

    // 사전 상세 정보 가져오기
    public DictDetailResponseDto getDictDetail(Long dictId, String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        Dict dict = getSafeDict(dictId);

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
        ValidChecker.loginCheck(userDetails);

        if (dictRepository.existsByDictName(dictPostRequestDto.getTitle())) {
            throw new IllegalArgumentException(EXIST_DICT);
        }
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        Dict dict = Dict.builder()
                .firstAuthor(user)
                .recentModifier(user)
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
        ValidChecker.loginCheck(userDetails);

        Dict dict = getSafeDict(dictId);

        DictHistory dictHistory = DictHistory.builder()
                .prevContent(dict.getContent())
                .user(dict.getRecentModifier())
                .dict(dict)
                .build();

        // 이전 내용 히스토리에 저장
        dictHistoryRepository.save(dictHistory);

        dict.setContent(dictPutRequestDto.getContent());

        return DictPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    // 사전 좋아요 / 좋아요 취소
    public DictLikeResponseDto likeDict(UserDetailsImpl userDetails, Long dictId) {
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Dict dict = getSafeDict(dictId);
        boolean isLike = false;
        if (isDictLike(dict, userDetails)) {
            DictLike dictLike = getSafeDictLike(user, dict);
            dictLikeRepository.deleteById(dictLike.getDictLikeId());
        } else {
            DictLike dictLike = DictLike.builder()
                    .dict(dict)
                    .user(user)
                    .build();
            dictLikeRepository.save(dictLike);
            isLike = true;
        }
        return DictLikeResponseDto.builder()
                .result(isLike)
                .build();
    }


    // region 보조 기능
    // Utils
    // 사전 좋아요 표시했는지 확인
    private boolean isDictLike(Dict dict, UserDetailsImpl userDetails) {
        // 1. 로그인하지 않았으면 무조건 false.
        // 2. dictLikeList 가 비어있으면 무조건 false.
        // 3. 사용자의 dictLike 목록에 해당 dict 가 포함되어있지 않으면 false.
        // 4. 포함되어있을시 true.
        if (userDetails == null) {
            return false;
        }

        for (DictLike dictLike : dict.getDictLikeList()) {
            if (dictLike.getUser().getUsername().equals(userDetails.getUsername())) {
                return true;
            }
        }

        return false;
    }

    // Get SafeEntity
    // Dict
    public Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        if (!dict.isPresent()) {
            throw new NullPointerException(NOT_EXIST_DICT);
        }

        return dict.get();
    }

    // DictPage
    private List<Dict> getSafeDictPage(int page, int size) {
        QDict qDict = QDict.dict;
        QDictLike qDictLike = QDictLike.dictLike;

        return queryFactory.selectFrom(qDict)
                .leftJoin(qDict.dictLikeList, qDictLike)
                .fetchJoin()
                .offset(page)
                .limit(size)
                .fetch();
    }

    // DictLike
    private DictLike getSafeDictLike(User user, Dict dict) {
        Optional<DictLike> dictLike = dictLikeRepository.findByUserAndDict(user, dict);
        if (!dictLike.isPresent()) {
            throw new NullPointerException(NOT_EXIST_DICT_LIKE);
        }

        return dictLike.get();
    }

    // Entity To Dto
    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Dict> dictList, UserDetailsImpl userDetails) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();

        for (Dict dict : dictList) {
            long start_time = System.currentTimeMillis();
            dictResponseDtoList.add(DictResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .meaning(dict.getContent())
                    .isLike(isDictLike(dict, userDetails))
                    .likeCount(dict.getDictLikeList().size())
                    .build());
            long end_time = System.currentTimeMillis();
            System.out.println("작업 수행 시간 : " + (end_time - start_time));
        }

        return dictResponseDtoList;
    }
    // endregion
}
