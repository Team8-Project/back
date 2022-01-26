package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.ViewersRepository;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.dict.DictYoutubeUrlRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dict.mymeme.DictMyMemeResponseDto;
import com.teamproj.backend.dto.dict.question.search.DictQuestionSearchResponseDto;
import com.teamproj.backend.dto.dict.search.DictSearchResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.dto.youtube.DictRelatedYoutubeDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.*;
import com.teamproj.backend.model.viewers.QViewers;
import com.teamproj.backend.model.viewers.ViewTypeEnum;
import com.teamproj.backend.model.viewers.Viewers;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.YoutubeService;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.MemegleServiceStaticMethods;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.util.RedisKey.BEST_DICT_KEY;
import static com.teamproj.backend.util.RedisKey.DICT_RECOMMEND_SEARCH_KEY;

@Service
@RequiredArgsConstructor
public class DictService {
    private final YoutubeService youtubeService;
    private final DictQuestionService dictQuestionService;

    private final DictRepository dictRepository;
    private final DictLikeRepository dictLikeRepository;
    private final ViewersRepository viewersRepository;
    private final DictYoutubeUrlRepository dictYoutubeUrlRepository;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    private final RedisService redisService;

    /**
     * 사전 목록 가져오기
     *
     * @param page  @RequestParam int page 페이지네이션용 페이지(offset)값. 현재 해당 기능의 페이지는 page*size 를 프론트엔드에서 요청해서 사용중임.
     * @param size  @RequestParam int size 페이지네이션용 사이즈(limit)값.
     * @param token Authorization header token.
     * @return DictList To DictResponseDto
     */
    public List<DictResponseDto> getDictList(int page, int size, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기 - 좋아요 했는지 여부 판단하기 위해 (select from user 시행 지점)
        User user = getSafeUserByUserDetails(userDetails);
        // 3. 사전 목록 가져오기 - 현재 페이지네이션이 잘못 되어있는데 프론트엔드 분들이 교정해서 쓰고 계셔서 수정 하지 않음.
        List<Tuple> dictTupleList = getSafeDictTupleList(page, size, user);
        // 4. 사전 목록을 알맞은 반환 양식으로 변환하여 return.
        return dictListToDictResponseDtoList(dictTupleList);
    }

    /**
     * 스크랩 목록 가져오기
     *
     * @param userDetails @AuthenticationPrincipal userDetails 정보.
     * @return DictList To DictMyMemeResponseDto
     */
    public List<DictMyMemeResponseDto> getMyMeme(UserDetailsImpl userDetails) {
        ValidChecker.loginCheck(userDetails);
        User user = getSafeUserByUserDetails(userDetails);
        List<Tuple> tupleList = getMyMemeList(user);
        return myMemeListToDictMyMemeResponseDtoList(tupleList);
    }

    /**
     * 사전 이름 중복검사
     *
     * @param dictName @RequestBody 정보값(사전 이름)
     * @return true : 사용 가능 / false : 사용 불가
     */
    public DictNameCheckResponseDto checkDictName(DictNameCheckRequestDto dictName) {
        return DictNameCheckResponseDto.builder()
                .result(!dictRepository.existsByDictName(dictName.getDictName()))
                .build();
    }

    /**
     * 사전 이름 중복검사 new : 사용 불가시 기존의 표현은 뭔지 나오도록.
     *
     * @param dictName @RequestBody 정보값(사전 이름)
     * @return Dict to DictNameCheckResponseDto
     */
    public DictNameCheckResponseDtoNeo neoCheckDictName(DictNameCheckRequestDto dictName) {
        Dict dict = dictRepository.findByDictName(dictName.getDictName());
        if (dict == null) {
            return DictNameCheckResponseDtoNeo.builder()
                    .result(true)
                    .build();
        }

        return DictNameCheckResponseDtoNeo.builder()
                .dictId(dict.getDictId())
                .dictName(dict.getDictName())
                .meaning(dict.getContent())
                .result(false)
                .build();
    }

    /**
     * 오늘의 밈 정보 가져오기
     *
     * @param token Authorization header token.
     * @return DictList To DictBestResponseDto
     */
    public List<DictBestResponseDto> getBestDict(String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기 - 좋아요 했는지 여부 판단하기 위해 (select from user 시행 지점)
        User user = getSafeUserByUserDetails(userDetails);
        // 3. 베스트 용어 사전 목록 가져오기 - 전날 가장 많이 열람한 게시글 목록을 출력. 부하 처리를 위해 Redis 에 0시에 캐싱.
        List<Dict> dictList = getSafeBestDict(BEST_DICT_KEY);
        // 4. 사전 목록을 알맞은 반환 양식으로 변환하여 return.
        return dictListToDictBestResponseDtoList(dictList, user);
    }

    /**
     * 사전 총 개수 출력
     * 사전 정보를 받아온 뒤 페이지네이션을 위해 사용.
     *
     * @param q @RequestParam String q 검색 쿼리값.
     * @return 사전 총 개수(쿼리값 존재할 경우 검색 결과의 총 개수)
     */
    public Long getDictTotalCount(String q) {
        // 1. 쿼리가 없을 경우 : 전체 사전의 개수 출력
        if (q == null) {
            return dictRepository.count();
        }
        // 2. 쿼리가 있을 경우 : 쿼리의 검색결과의 개수 출력
        return dictRepository.countByDictNameContainingOrContentContaining(q, q);
    }

    /**
     * 사전 상세정보
     *
     * @param dictId @PathVariable Long dictId
     * @param token  Authorization header token
     * @return DictDetailResponseDto
     */
    public DictDetailResponseDto getDictDetail(Long dictId, String token, String viewerIp) {
        // 1. 로그인한 사용자일 경우 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = getSafeUserByUserDetails(userDetails);
        // 2. 사전 정보 받아오기
        Tuple dictTuple = getSafeDictTuple(dictId, user, viewerIp);
        // 3. 알맞은 DTO 형식으로 전환.
        DictDetailResponseDto result = dictTupleToDictDetailResponseDto(dictId, dictTuple);
        // 4. 조회수 증가 여부 판단 후 증가.
        viewProc(dictTuple, viewerIp);
        // 5. 반환.
        return result;
    }

    /**
     * 사전 작성하기
     *
     * @param userDetails        @AuthenticationPrincipal UserDetailsImpl userDetails
     * @param dictPostRequestDto @RequestBody 정보값
     * @return DictPostResponseDto
     */
    @Transactional
    public DictPostResponseDto postDict(UserDetailsImpl userDetails, DictPostRequestDto dictPostRequestDto) {
        // 비회원이 함수로 요청하는지 확인(JWT 토큰의 유효성으로 확인)
        ValidChecker.loginCheck(userDetails);

        String dictName = dictPostRequestDto.getTitle();
        String summary = dictPostRequestDto.getSummary();
        String content = dictPostRequestDto.getContent();

        // 한줄요약이 너무 길 경우 예외 발생.
        if (summary.length() > 30) {
            throw new IllegalArgumentException(SUMMARY_IS_TOO_BIG);
        }

        // 이미 같은 이름의 사전이 존재할 경우 예외 발생.
        if (dictRepository.existsByDictName(dictName)) {
            throw new IllegalArgumentException(EXIST_DICT);
        }
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        // 최초 작성자와 최근 수정자는 우선 동일하게 부여함.
        Dict dict = Dict.builder()
                .firstAuthor(user)
                .recentModifier(user)
                .content(content)
                .dictName(dictName)
                .summary(summary)
                .build();

        // 최초 사전 수정내역 생성 후 삽입
        DictHistory dictHistory = DictHistory.builder()
                .prevSummary(dict.getSummary())
                .prevContent(dict.getContent())
                .user(user)
                .dict(dict)
                .build();
        dict.addHistory(dictHistory);

        // 연관동영상 리스트 받아온 후 삽입
        List<DictYoutubeUrl> dictYoutubeUrlList = youtubeService.getYoutubeSearchResult(dict, dictName);
        if (dictYoutubeUrlList.size() > 0) {
            for (DictYoutubeUrl dictYoutubeUrl : dictYoutubeUrlList) {
                dict.addDictYoutubeUrl(dictYoutubeUrl);
            }
        }

        // 사전 저장
        dictRepository.save(dict);

        return DictPostResponseDto.builder()
                .result("작성 성공")
                .build();
    }

    /**
     * 사전 수정하기 및 수정 내역에 저장
     *
     * @param userDetails       @AuthenticationPrincipal UserDetailsImpl userDetails
     * @param dictId            @PathVariable Long dictId
     * @param dictPutRequestDto @RequestBody 정보값
     * @return String "수정 완료"
     */
    @Transactional
    public DictPutResponseDto putDict(UserDetailsImpl userDetails, Long dictId, DictPutRequestDto dictPutRequestDto) {
        ValidChecker.loginCheck(userDetails);

        String summary = dictPutRequestDto.getSummary();
        String content = dictPutRequestDto.getContent();

        // 한줄요약이 너무 길 경우 예외 발생.
        if (summary.length() > 30) {
            throw new IllegalArgumentException(SUMMARY_IS_TOO_BIG);
        }

        Dict dict = getSafeDict(dictId);
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        // 이전 내용 히스토리에 저장
        DictHistory dictHistory = DictHistory.builder()
                .prevSummary(dict.getSummary())
                .prevContent(dict.getContent())
                .user(user)
                .dict(dict)
                .build();
        dict.addHistory(dictHistory);

        dict.setRecentModifier(user);
        dict.setSummary(summary);
        dict.setContent(content);

        return DictPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    /**
     * 사전 좋아요 / 좋아요 취소
     *
     * @param userDetails @AuthenticationPrincipal UserDetailsImpl userDetails
     * @param dictId      @PathVariable Long dictId
     * @return true : 좋아요 완료 / false : 좋아요 취소 완료
     */
    @Transactional
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
        Optional<DictLike> dictLike = dictLikeRepository.findByUserAndDict(user, dict);
        if (dictLike.isPresent()) {
            dictLikeRepository.deleteById(dictLike.get().getDictLikeId());
        } else {
            DictLike newDictLike = DictLike.builder()
                    .dict(dict)
                    .user(user)
                    .build();
            dictLikeRepository.save(newDictLike);
            isLike = true;
        }
        return DictLikeResponseDto.builder()
                .result(isLike)
                .build();
    }

    // 추천 검색어 기능. 현재 활용되지 않고 있음.
//    public List<String> getSearchInfo() {
//        List<String> result = getSafeRecommendSearch(DICT_RECOMMEND_SEARCH_KEY);
//
//        Collections.shuffle(result);
//        int returnSize = Math.min(result.size(), 7);
//        return result.subList(0, returnSize);
//    }

    /**
     * 검색 기능
     *
     * @param token Authentication header token
     * @param q     @RequestParam String q 쿼리값
     * @param page  @RequestParam int page 페이지값
     * @param size  @RequestParam int size 사이즈값
     * @return DictSearchResponseDto
     */
    public DictSearchResponseDto getSearchResult(String token, String q, int page, int size) {
        // 검색 결과를 좋아요 했는지 확인하기 위해 사용자 정보를 받음.
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = getSafeUserByUserDetails(userDetails);

        List<DictSearchResultResponseDto> dictResult = getDictSearchResult(user, q, page, size);
        List<DictQuestionSearchResponseDto> questionResult = dictQuestionService.questionSearch(user, q, page, size);

        return DictSearchResponseDto.builder()
                .dictResult(dictResult)
                .questionResult(questionResult)
                .build();
    }


    // region 보조 기능
    // Utils

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

        return recommend;
    }

    // 오늘의 밈 출력
    public List<MainTodayMemeResponseDto> getTodayMeme(int size) {
        // 1. 좋아요 테이블에서 각 좋아요 개수 불러와서 내림차순으로 정렬
        // 2. 그 중 20개정도 뽑아서 섞은다음 7개 찾아서 그걸로 탐색하고 정렬
        // 3. 받아와서 MainTodayMemeResponseDto 에 스택
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

    // 사전 상세보기시 조회수 증가 로직
    private void viewProc(Tuple dictTuple, String viewerIp) {
        Long dictId = dictTuple.get(0, Long.class);
        Long viewerIpLong = dictTuple.get(12, Long.class);
        boolean isView = viewerIpLong != null && viewerIpLong > 0;

        // 조회수 증가
        // 1. 조회수 테이블 열람
        // 2. 조회수 테이블에 내가 확인했다는 기록(IP로 판단)이 존재할 경우 조회수 상승하지 않음
        // 3. 존재하지 않을 경우 조회수 상승하고 조회수 테이블에 사용자 정보 등록.
        // 조회수 테이블은 매일 0시에 초기화 됨.
        if (!isView) {
            viewersRepository.save(Viewers.builder()
                    .viewTypeEnum(ViewTypeEnum.DICT)
                    .targetId(dictId)
                    .viewerIp(viewerIp)
                    .build());
            dictRepository.updateView(dictId);
        }
    }

    // 좋아요 목록 가져와서 HashMap 으로 반환
    public HashMap<String, Boolean> getDictLikeMap(List<Long> dictIdList, User user) {
        QDictLike qDictLike = QDictLike.dictLike;
        List<Tuple> dictLikeListTuple = queryFactory
                .select(qDictLike.dict.dictId, qDictLike.user.id)
                .from(qDictLike)
                .where(eqUser(user),
                        qDictLike.dict.dictId.in(dictIdList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(dictLikeListTuple);
    }

    // qDictLike 와 user 를 비교하기 위한 BooleanExpression.
    private BooleanExpression eqUser(User user) {
        return user == null ? QDictLike.dictLike.dictLikeId.eq(0L) : QDictLike.dictLike.user.eq(user);
    }

    /**
     * 검색 결과값 받아오기. getSearchResult 메소드의 보조 기능.
     *
     * @param user jwtAuthenticateProcessor.getUser(userDetails) 결과값. @Nullable
     * @param q    쿼리값
     * @param page 페이지값
     * @param size 사이즈값
     * @return DictList To DictSearchResultResponseDto
     */
    public List<DictSearchResultResponseDto> getDictSearchResult(User user, String q, int page, int size) {
        if (q.length() < 1) {
            return new ArrayList<>();
        }
        List<Tuple> searchResultTupleList = getSearchResultTupleList(q, page, size);

        return dictListToDictSearchResultResponseDto(searchResultTupleList, user);
    }

    // Get SafeEntity
    // User By UserDetails
    private User getSafeUserByUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return null;
        }
        return jwtAuthenticateProcessor.getUser(userDetails);
    }

    // MyMemeList By User
    private List<Tuple> getMyMemeList(User user) {
        QDictLike qDictLike = QDictLike.dictLike;
        return queryFactory
                .select(qDictLike.dict.dictId, qDictLike.dict.dictName, qDictLike.dict.content, qDictLike.dict.summary)
                .from(qDictLike)
                .where(qDictLike.user.eq(user))
                .orderBy(qDictLike.createdAt.desc())
                .fetch();
    }

    // Dict
    public Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        return dict.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT));
    }

    // DictTupleList
    private List<Tuple> getSafeDictTupleList(int page, int size, User user) {
        QDict qDict = QDict.dict;
        QDictLike qDictLike = QDictLike.dictLike;

        // 원래 정석은 offset 은 page * size 로 줘야함..... 실수했는데 프론트분들이 이대로 작업하셔서 수정하지 않고 사용하기로 함
        return queryFactory
                .select(qDict.dictId,
                        qDict.dictName,
                        qDict.summary,
                        qDict.firstAuthor.nickname,
                        qDict.createdAt,
                        qDict.dictLikeList.size(),
                        queryFactory
                                .select(qDictLike.count())
                                .from(qDictLike)
                                .where(eqUser(user),
                                        qDictLike.dict.eq(qDict))
                )
                .from(qDict)
                .orderBy(qDict.createdAt.desc())
                .offset(page)
                .limit(size)
                .fetch();
    }

    // DictLikeTuple
    private List<Tuple> getSafeDictLikeCountTupleOrderByDescLimit(int size) {
        QDictLike qDictLike = QDictLike.dictLike;

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory
                .select(qDictLike.dict.dictId, qDictLike.dict.dictName, qDictLike.dict.count().as(count))
                .from(qDictLike)
                .groupBy(qDictLike.dict)
                .orderBy(count.desc())
                .limit(size)
                .fetch();
    }

    // BestDict
    public List<String> getSafeBestDict() {
        List<Tuple> tupleList = getSafeBestDictTuple();

        List<String> idList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            idList.add(String.valueOf(tuple.get(0, Long.class)));
        }
        return idList;
    }

    // DictBestTuple
    private List<Tuple> getSafeBestDictTuple() {
        QViewers qViewers = QViewers.viewers;

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory
                .select(qViewers.targetId, qViewers.count().as(count))
                .from(qViewers)
                .where(qViewers.viewTypeEnum.eq(ViewTypeEnum.DICT))
                .groupBy(qViewers.targetId)
                .orderBy(count.desc())
                .limit(20)
                .fetch();
    }

    // RecommendSearch
    private List<String> getSafeRecommendSearch(String key) {
        try {
            List<String> result = redisService.getStringList(key);

            if (result == null) {
                redisService.setRecommendSearch(key, getRecommendSearch(20));
                result = redisService.getStringList(key);
                if (result == null) {
                    return new ArrayList<>();
                }
            }
            return result;
        } catch (RedisConnectionFailureException e) {
            return getRecommendSearch(20);
        }
    }

    // BestDict
    private List<Dict> getSafeBestDict(String key) {
        List<String> bestDictIdList;
        try {
            bestDictIdList = redisService.getStringList(key);

            if (bestDictIdList == null) {
                redisService.setBestDict(key, getSafeBestDict());
                bestDictIdList = redisService.getStringList(key);

                if (bestDictIdList == null) {
                    return dictRepository.findAllByOrderByViewsDesc(PageRequest.of(0, 5)).toList();
                }
            }
        } catch (RedisConnectionFailureException e) {
            bestDictIdList = getSafeBestDict();
        }


        Collections.shuffle(bestDictIdList);
        List<Long> nums = new ArrayList<>();
        for (String str : bestDictIdList.subList(0, Math.min(5, bestDictIdList.size()))) {
            nums.add(Long.parseLong(str));
        }

        Optional<List<Dict>> dictList = dictRepository.findAllByDictIdIn(nums);
        return dictList.orElseGet(ArrayList::new);
    }

    // DictList 검색결과
    private List<Tuple> getSearchResultTupleList(String q, int page, int size) {
        QDict qDict = QDict.dict;
        String newQ = "%" + q + "%";
        int offset = page * size;

        return queryFactory
                .select(qDict.dictId, qDict.dictName, qDict.summary, qDict.content, qDict.firstAuthor.nickname,
                        qDict.createdAt, qDict.dictLikeList.size())
                .from(qDict)
                .where(qDict.dictName.like(newQ)
                        .or(qDict.summary.like(newQ))
                        .or(qDict.content.like(newQ)))
                .orderBy(qDict.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    // 사전 상세정보 Tuple
    private Tuple getSafeDictTuple(Long dictId, User user, String userIp) {
        QDict qDict = QDict.dict;
        QDictLike qDictLike = QDictLike.dictLike;
        QViewers qViewers = QViewers.viewers;

        Tuple result = queryFactory
                .select(qDict.dictId.as("id"),
                        qDict.dictName.as("title"),
                        qDict.summary.as("summary"),
                        qDict.content.as("meaning"),
                        qDict.firstAuthor.nickname.as("first_author_name"),
                        qDict.firstAuthor.profileImage.as("first_author_profile_image"),
                        qDict.recentModifier.nickname.as("recent_modifier_name"),
                        qDict.recentModifier.profileImage.as("recent_modifier_profile_image"),
                        queryFactory
                                .select(qDictLike.dictLikeId.count())
                                .from(qDictLike)
                                .where(qDictLike.dict.eq(qDict),
                                        isLikeExp(user)),
                        qDict.dictLikeList.size().as("like_count"),
                        qDict.createdAt.as("created_at"),
                        qDict.modifiedAt.as("modified_at"),
                        queryFactory
                                .select(qViewers.count())
                                .from(qViewers)
                                .where(qViewers.targetId.eq(dictId),
                                        qViewers.viewerIp.eq(userIp),
                                        qViewers.viewTypeEnum.eq(ViewTypeEnum.DICT))
                )
                .from(qDict)
                .where(qDict.dictId.eq(dictId))
                .fetchFirst();

        if (result == null) {
            throw new NullPointerException(NOT_EXIST_DICT);
        }

        return result;
    }

    // 좋아요 어부 확인 BooleanExpression
    private BooleanExpression isLikeExp(User user) {
        return user == null ? QDictLike.dictLike.dictLikeId.eq(0L) : QDictLike.dictLike.user.eq(user);
    }

    // Entity To Dto
    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Tuple> dictTupleList) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();

        for (Tuple tuple : dictTupleList) {
            Long dictId = tuple.get(0, Long.class);
            String title = tuple.get(1, String.class);
            String summary = tuple.get(2, String.class);
            String firstWriter = tuple.get(3, String.class);
            LocalDateTime createdAt = tuple.get(4, LocalDateTime.class);
            Integer likeCountInteger = tuple.get(5, Integer.class);
            int likeCount = likeCountInteger == null ? 0 : likeCountInteger;
            Long isDictLikeLong = tuple.get(6, Long.class);
            boolean isDictLike = isDictLikeLong != null && isDictLikeLong > 0;

            dictResponseDtoList.add(DictResponseDto.builder()
                    .dictId(dictId)
                    .title(title)
                    .summary(summary)
                    .firstWriter(firstWriter)
                    .createdAt(createdAt)
                    .isLike(isDictLike)
                    .likeCount(likeCount)
                    .build());
        }

        return dictResponseDtoList;
    }

    public HashMap<Long, Long> getLikeCountMap(List<Dict> dictList) {
        QDictLike qDictLike = QDictLike.dictLike;
        QDict qDict = QDict.dict;

        List<Tuple> likeCountListTuple = queryFactory
                .select(qDictLike.dict.dictId, qDictLike.count())
                .from(qDictLike)
                .where(qDictLike.dict.in(dictList))
                .groupBy(qDict)
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(likeCountListTuple);
    }


    // DictDtoList to DictSearchResultResponseDtoList
    private List<DictSearchResultResponseDto> dictListToDictSearchResultResponseDto(List<Tuple> dictTupleList, User user) {
        List<DictSearchResultResponseDto> dictSearchResultResponseDto = new ArrayList<>();

        // 좋아요 맵
        List<Long> dictIdList = getDictIdListByTupleList(dictTupleList);
        HashMap<String, Boolean> dictLikeMap = getDictLikeMap(dictIdList, user);

        for (Tuple tuple : dictTupleList) {
            Long dictId = tuple.get(0, Long.class);
            String title = tuple.get(1, String.class);
            String summary = tuple.get(2, String.class);
            String meaning = tuple.get(3, String.class);
            String firstWriter = tuple.get(4, String.class);
            LocalDateTime createdAt = tuple.get(5, LocalDateTime.class);
            Integer likeCountInteger = tuple.get(6, Integer.class);
            int likeCount = likeCountInteger == null ? 0 : likeCountInteger;

            dictSearchResultResponseDto.add(DictSearchResultResponseDto.builder()
                    .dictId(dictId)
                    .title(title)
                    .summary(summary)
                    .meaning(meaning)
                    .firstWriter(firstWriter)
                    .createdAt(createdAt)
                    .isLike(user != null && dictLikeMap.get(dictId + ":" + user.getId()) != null)
                    .likeCount(likeCount)
                    .build());
        }

        return dictSearchResultResponseDto;
    }

    private List<Long> getDictIdListByTupleList(List<Tuple> tupleList) {
        List<Long> result = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            result.add(tuple.get(0, Long.class));
        }
        return result;
    }

    // DictList to DictBestResponseDtoList
    public List<DictBestResponseDto> dictListToDictBestResponseDtoList(List<Dict> dictList, User user) {
        List<DictBestResponseDto> dictBestResponseDtoList = new ArrayList<>();

        // 좋아요 맵
        List<Long> li = new ArrayList<>();
        for (Dict dict : dictList) {
            li.add(dict.getDictId());
        }
        HashMap<String, Boolean> dictLikeMap = getDictLikeMap(li, user);
        // 좋아요 개수 맵
        HashMap<Long, Long> likeCountMap = getLikeCountMap(dictList);

        for (Dict dict : dictList) {
            // likeCountMap 에 값이 없을경우 좋아요가 없음 = 0개.
            int likeCount = likeCountMap.get(dict.getDictId()) == null ? 0 : likeCountMap.get(dict.getDictId()).intValue();

            dictBestResponseDtoList.add(DictBestResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .summary(dict.getSummary())
                    .meaning(dict.getContent())
                    .isLike(user != null && dictLikeMap.get(dict.getDictId() + ":" + user.getId()) != null)
                    .likeCount(likeCount)
                    .build());
        }
        return dictBestResponseDtoList;
    }

    // DictYoutubeUrlList To DictRelatedYoutubeDtoList
    private List<DictRelatedYoutubeDto> getDictYoutubeUrlListToDictRelatedYoutubeDtoList(List<DictYoutubeUrl> dictYoutubeUrlList) {
        List<DictRelatedYoutubeDto> result = new ArrayList<>();

        for (DictYoutubeUrl dictYoutubeUrl : dictYoutubeUrlList) {
            result.add(DictRelatedYoutubeDto.builder()
                    .title(dictYoutubeUrl.getTitle())
                    .thumbNail(dictYoutubeUrl.getThumbNail())
                    .channel(dictYoutubeUrl.getChannel())
                    .youtubeId(dictYoutubeUrl.getYoutubeUrl())
                    .build());
        }

        return result;
    }

    // MyMemeList To DictMyMemeResponseDtoList
    public List<DictMyMemeResponseDto> myMemeListToDictMyMemeResponseDtoList(List<Tuple> tupleList) {
        List<DictMyMemeResponseDto> dictMyMemeResponseDtoList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            dictMyMemeResponseDtoList.add(DictMyMemeResponseDto.builder()
                    .dictId(tuple.get(0, Long.class))
                    .title(tuple.get(1, String.class))
                    .meaning(tuple.get(2, String.class))
                    .summary(tuple.get(3, String.class))
                    .build());
        }

        return dictMyMemeResponseDtoList;
    }

    // DictTuple To DictDetailResponseDto
    private DictDetailResponseDto dictTupleToDictDetailResponseDto(Long dictId, Tuple dictTuple) {
        String title = dictTuple.get(1, String.class);
        String summary = dictTuple.get(2, String.class);
        String meaning = dictTuple.get(3, String.class);
        String firstWriterNickname = dictTuple.get(4, String.class);
        String firstWriterProfileImage = dictTuple.get(5, String.class);
        String recentWriterNickname = dictTuple.get(6, String.class);
        String recentWriterProfileImage = dictTuple.get(7, String.class);
        Long isLikeLong = dictTuple.get(8, Long.class);
        boolean isLike = isLikeLong != null && isLikeLong > 0;
        Integer likeCountInteger = dictTuple.get(9, Integer.class);
        int likeCount = likeCountInteger == null ? 0 : likeCountInteger;
        LocalDateTime createdAt = dictTuple.get(10, LocalDateTime.class);
        LocalDateTime modifiedAt = dictTuple.get(11, LocalDateTime.class);

        // 사용자 정보가 존재할 경우 좋아요 여부 감별 실시.
        List<DictYoutubeUrl> dictYoutubeUrlList = dictYoutubeUrlRepository.findAllByDict_DictId(dictId);
        List<DictRelatedYoutubeDto> dictRelatedYoutubeDtoList = getDictYoutubeUrlListToDictRelatedYoutubeDtoList(dictYoutubeUrlList);
        return DictDetailResponseDto.builder()
                .dictId(dictId)
                .title(title)
                .summary(summary)
                .meaning(meaning)
                .firstWriter(firstWriterNickname)
                .firstWriterProfileImage(firstWriterProfileImage)
                .recentWriter(recentWriterNickname)
                .recentWriterProfileImage(recentWriterProfileImage)
                .isLike(isLike)
                .likeCount(likeCount)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .relatedYoutube(dictRelatedYoutubeDtoList)
                .build();
    }
    // endregion
}