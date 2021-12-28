package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.RecentSearchRepository;
import com.teamproj.backend.Repository.dict.DictHistoryRepository;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.QueryTypeEnum;
import com.teamproj.backend.model.RecentSearch;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
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
    private final RecentSearchRepository recentSearchRepository;

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
                .summary(dict.getSummary())
                .meaning(dict.getContent())
                .firstWriter(dict.getFirstAuthor().getNickname())
                .recentWriter(dict.getRecentModifier().getNickname())
                .isLike(isDictLike(dict, userDetails))
                .likeCount(dict.getDictLikeList().size())
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
                .summary(dictPostRequestDto.getSummary())
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
                .prevSummary(dict.getSummary())
                .prevContent(dict.getContent())
                .user(dict.getRecentModifier())
                .dict(dict)
                .build();

        // 이전 내용 히스토리에 저장
        dictHistoryRepository.save(dictHistory);

        dict.setSummary(dictPutRequestDto.getSummary());
        dict.setContent(dictPutRequestDto.getContent());

        return DictPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    // 사전 좋아요 / 좋아요 취소
    public DictLikeResponseDto likeDict(UserDetailsImpl userDetails, Long dictId) {
        // 로그인 체크
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Dict dict = getSafeDict(dictId);

        /*
            1. 좋아요 중일 시 : 좋아요 취소
            2. 좋아요 중이 아닐 시 : 좋아요
         */
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

    public List<String> getSearchInfo() {
        /*
            최근 검색어 기능은 프론트엔드에서 구현할 수 있는지 여부 검증되기 전까지
            비활성화 상태로 유지합니다.
        */
        return getRecommendSearch(20);
    }

//        public DictSearchInfoResponseDto getSearchInfo(UserDetailsImpl userDetails) {
//        ValidChecker.loginCheck(userDetails);

//        User user = jwtAuthenticateProcessor.getUser(userDetails);

//        List<String> recent = getRecentSearch(user);
//        List<String> recommend = getRecommendSearch(20);

//        return DictSearchInfoResponseDto.builder()
//                .recent(recent)
//                .recommend(recommend)
//                .build();
//    }


    public List<DictSearchResultResponseDto> getSearchResult(String token, String q, int page, int size) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);

        List<Dict> dictList = getSafeDictListBySearch(q, page, size);

        return dictListToDictSearchResultResponseDto(dictList, userDetails);
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
        Optional<DictLike> dictLike = dictLikeRepository.findByUserAndDict(jwtAuthenticateProcessor.getUser(userDetails), dict);

        return dictLike.isPresent();
    }

    // 사전 추천 검색어 출력
    public List<String> getRecommendSearch(int size) {
        // 1. 좋아요 테이블에서 각 좋아요 개수 불러와서 내림차순으로 정렬
        // 2. 그 중 20개정도 뽑아서 섞은다음 7개 찾아서 그걸로 탐색하고 정렬
        // 3. good!
        List<Tuple> tupleList = getSafeDictLikeCountTupleOrderByDescLimit(size);
        List<String> recommend = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            recommend.add(tuple.get(1, String.class));
        }

        Collections.shuffle(recommend);
        int returnSize = Math.min(recommend.size(), 7);
        return recommend.subList(0, returnSize);
    }

    // 오늘의 밈 출력
    public List<MainTodayMemeResponseDto> getTodayMeme(int size) {
        // 1. 좋아요 테이블에서 각 좋아요 개수 불러와서 내림차순으로 정렬
        // 2. 그 중 20개정도 뽑아서 섞은다음 7개 찾아서 그걸로 탐색하고 정렬
        // 3. 받아와서 MainTodayMemeResponseDto에 스택
        // 4. good!
        List<Tuple> tupleList = getSafeDictLikeCountTupleOrderByDescLimit(size);
        List<MainTodayMemeResponseDto> recommend = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            recommend.add(MainTodayMemeResponseDto.builder()
                    .dictId(tuple.get(0, Long.class))
                    .dictName(tuple.get(1, String.class))
                    .build());
        }

        return recommend;
    }

    // 최근 검색어 출력
//    private List<String> getRecentSearch(User user) {
//        List<RecentSearch> recentSearchList = getSafeRecentSearchList(user, QueryTypeEnum.DICT);
//
//        List<String> recent = new ArrayList<>();
//
//        for (RecentSearch recentSearch : recentSearchList) {
//            recent.add(recentSearch.getQuery());
//        }
//
//        return recent;
//    }

    // Get SafeEntity
    // Dict
    public Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        return dict.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT));
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
        return dictLike.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT_LIKE));
    }

    // RecentSearchList
    private List<RecentSearch> getSafeRecentSearchList(User user, QueryTypeEnum type) {
        Optional<List<RecentSearch>> recentSearchList = recentSearchRepository.findAllByUserAndTypeOrderByCreatedAtDesc(user, type);
        return recentSearchList.orElseGet(ArrayList::new);
    }

    // DictLikeTuple
    private List<Tuple> getSafeDictLikeCountTupleOrderByDescLimit(int size) {
        QDictLike qDictLike = QDictLike.dictLike;

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory.select(qDictLike.dict.dictId, qDictLike.dict.dictName, qDictLike.dict.count().as(count))
                .from(qDictLike)
                .groupBy(qDictLike.dict)
                .orderBy(count.desc())
                .limit(size)
                .fetch();
    }

    // DictList 검색결과
    private List<Dict> getSafeDictListBySearch(String q, int page, int size) {
        String queryString = "%" + q + "%";
        Optional<Page<Dict>> searchResult = dictRepository.findAllByDictNameLikeOrContentLike(queryString, queryString, PageRequest.of(page, size));
        return searchResult.map(Streamable::toList).orElseGet(ArrayList::new);
    }

    // Entity To Dto
    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Dict> dictList, UserDetailsImpl userDetails) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();

        for (Dict dict : dictList) {
            dictResponseDtoList.add(DictResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .summary(dict.getSummary())
                    .meaning(dict.getContent())
                    .isLike(isDictLike(dict, userDetails))
                    .likeCount(dict.getDictLikeList().size())
                    .build());
        }

        return dictResponseDtoList;
    }

    // DictDtoList to DictSearchResultResponseDtoList
    private List<DictSearchResultResponseDto> dictListToDictSearchResultResponseDto(List<Dict> dictList, UserDetailsImpl userDetails) {
        List<DictSearchResultResponseDto> dictSearchResultResponseDto = new ArrayList<>();

        for (Dict dict : dictList) {
            dictSearchResultResponseDto.add(DictSearchResultResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .summary(dict.getSummary())
                    .meaning(dict.getContent())
                    .isLike(isDictLike(dict, userDetails))
                    .likeCount(dict.getDictLikeList().size())
                    .build());
        }

        return dictSearchResultResponseDto;
    }

    // endregion
}