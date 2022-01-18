package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.dict.DictViewersRepository;
import com.teamproj.backend.dto.dict.*;
import com.teamproj.backend.dto.dict.mymeme.DictMyMemeResponseDto;
import com.teamproj.backend.dto.dict.question.search.DictQuestionSearchResponseDto;
import com.teamproj.backend.dto.dict.search.DictSearchResponseDto;
import com.teamproj.backend.dto.main.MainTodayMemeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.YoutubeService;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.MemegleServiceStaticMethods;
import com.teamproj.backend.util.StatisticsUtils;
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
    private final DictHistoryService dictHistoryService;

    private final DictRepository dictRepository;
    private final DictLikeRepository dictLikeRepository;
    private final DictViewersRepository dictViewersRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    private final RedisService redisService;

    // 사전 목록 가져오기
    public List<DictResponseDto> getDictList(int page, int size, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기 - 좋아요 했는지 여부 판단하기 위해 (select from user 시행 지점)
        User user = getSafeUserByUserDetails(userDetails);
        // 3. 사전 목록 가져오기 - 현재 페이지네이션이 잘못 되어있는데 프론트엔드 분들이 교정해서 쓰고 계셔서 수정 하지 않음.
        List<Tuple> dictTupleList = getSafeDictTupleList(page, size);
        // 4. 사전 목록을 알맞은 반환 양식으로 변환하여 return.
        return dictListToDictResponseDtoList(dictTupleList, user);
    }

    // 스크랩 목록 가져오기
    public List<DictMyMemeResponseDto> getMyMeme(UserDetailsImpl userDetails) {
        ValidChecker.loginCheck(userDetails);
        User user = getSafeUserByUserDetails(userDetails);

        return getMyMemeList(user);
    }

    // 사전 이름 중복검사
    public DictNameCheckResponseDto checkDictName(DictNameCheckRequestDto dictName) {
        return DictNameCheckResponseDto.builder()
                .result(!dictRepository.existsByDictName(dictName.getDictName()))
                .build();
    }

    // 사전 이름 중복검사. 사용불가시 기존 표현 뭔지 나오도록.
    public DictNameCheckResponseDtoNeo neoCheckDictName(DictNameCheckRequestDto dictName) {
        Dict dict = dictRepository.findByDictName(dictName.getDictName());
        if (dict == null) {
            return DictNameCheckResponseDtoNeo.builder()
                    .result(false)
                    .build();
        }

        return DictNameCheckResponseDtoNeo.builder()
                .dictId(dict.getDictId())
                .dictName(dict.getDictName())
                .meaning(dict.getContent())
                .result(true)
                .build();
    }

    // 베스트 용어 사전 가져오기
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

    // 사전 총 개수 출력
    // 프론트엔드의 총 페이지 출력을 위해.
    public Long getDictTotalCount(String q) {
        // 1. 쿼리가 없을 경우 : 전체 사전의 개수 출력
        if (q == null) {
            return dictRepository.count();
        }
        // 2. 쿼리가 있을 경우 : 쿼리의 검색결과의 개수 출력
//        String query = q + "*";
//        return dictRepository.countByDictNameOrContentByFullText(query);
        return dictRepository.count();
    }

    // 사전 상세 정보 가져오기
    public DictDetailResponseDto getDictDetail(Long dictId, String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = getSafeUserByUserDetails(userDetails);
        Dict dict = getSafeDict(dictId);

        // 조회수 증가
        // 1. 조회수 테이블 열람
        // 2. 조회수 테이블에 내가 확인했다는 기록(IP로 판단)이 존재할 경우 조회수 상승하지 않음
        // 3. 존재하지 않을 경우 조회수 상승하고 조회수 테이블에 사용자 정보 등록.
        // 조회수 테이블은 매일 0시에 초기화 됨.
        if (!isView(dict)) {
            dictViewersRepository.save(DictViewers.builder()
                    .viewerIp(StatisticsUtils.getClientIp())
                    .dict(dict)
                    .build());
            dictRepository.updateView(dictId);
        }

        // 사용자 정보가 존재할 경우 좋아요 여부 감별 실시.
        User firstWriter = dict.getFirstAuthor();
        User recentWriter = dict.getRecentModifier();
        return DictDetailResponseDto.builder()
                .dictId(dict.getDictId())
                .title(dict.getDictName())
                .summary(dict.getSummary())
                .meaning(dict.getContent())
                .firstWriter(firstWriter.getNickname())
                .firstWriterProfileImage(firstWriter.getProfileImage())
                .recentWriter(recentWriter.getNickname())
                .recentWriterProfileImage(recentWriter.getProfileImage())
                .isLike(user != null && isDictLike(dict, user))
                .likeCount(dict.getDictLikeList().size())
                .createdAt(dict.getCreatedAt())
                .modifiedAt(dict.getModifiedAt())
                .build();
    }

    // 사전 작성하기
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

    // 사전 수정하기 및 수정 내역에 저장
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

    // 사전 좋아요 / 좋아요 취소
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
    public List<String> getSearchInfo() {
        List<String> result = getSafeRecommendSearch(DICT_RECOMMEND_SEARCH_KEY);

        Collections.shuffle(result);
        int returnSize = Math.min(result.size(), 7);
        return result.subList(0, returnSize);
    }

    // 검색 기능
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

    // 사전 검색 기능
    public List<DictSearchResultResponseDto> getDictSearchResult(User user, String q, int page, int size) {
        if (q.length() < 1) {
            return new ArrayList<>();
        }
        List<Tuple> searchResultTupleList = getSearchResultTupleList(q, page, size);

        return dictListToDictSearchResultResponseDto(searchResultTupleList, user);
    }

    // region 보조 기능
    // Utils
    // 사전 상세보기 열람했는지 확인
    private boolean isView(Dict dict) {
        return dictViewersRepository.existsByViewerIpAndDict(StatisticsUtils.getClientIp(), dict);
    }

    // 사전 좋아요 표시했는지 확인
    private boolean isDictLike(Dict dict, User user) {
        // 1. 로그인하지 않았으면 무조건 false.
        // 2. dictLikeList 가 비어있으면 무조건 false.
        // 3. 사용자의 dictLike 목록에 해당 dict 가 포함되어있지 않으면 false.
        // 4. 포함되어있을시 true.
        for (DictLike dictLike : dict.getDictLikeList()) {
            if (dictLike.getUser().getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
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

    // 좋아요 목록 가져와서 HashMap 으로 반환
    public HashMap<String, Boolean> getDictLikeMap(List<Long> dictIdList) {
        QDictLike qDictLike = QDictLike.dictLike;
        List<Tuple> dictLikeListTuple = queryFactory
                .select(qDictLike.dict.dictId, qDictLike.user.id)
                .from(qDictLike)
                .where(qDictLike.dict.dictId.in(dictIdList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(dictLikeListTuple);
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
    private List<DictMyMemeResponseDto> getMyMemeList(User user) {
        QDictLike qDictLike = QDictLike.dictLike;
        List<Tuple> tupleList = queryFactory
                .select(qDictLike.dict.dictId, qDictLike.dict.dictName, qDictLike.dict.content, qDictLike.dict.summary)
                .from(qDictLike)
                .where(qDictLike.user.eq(user))
                .orderBy(qDictLike.createdAt.desc())
                .fetch();

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

    // Dict
    public Dict getSafeDict(Long dictId) {
        Optional<Dict> dict = dictRepository.findById(dictId);
        return dict.orElseThrow(() -> new NullPointerException(NOT_EXIST_DICT));
    }

    // DictTupleList
    private List<Tuple> getSafeDictTupleList(int page, int size) {
        QDict qDict = QDict.dict;

        // 원래 정석은 offset 은 page * size 로 줘야함..... 실수했는데 프론트분들이 이대로 작업하셔서 수정하지 않고 사용하기로 함
        return queryFactory
                .select(qDict.dictId, qDict.dictName, qDict.summary, qDict.firstAuthor.nickname, qDict.createdAt, qDict.dictLikeList.size())
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
        QDictViewers qDictViewers = QDictViewers.dictViewers;

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory
                .select(qDictViewers.dict.dictId, qDictViewers.dict.count().as(count))
                .from(qDictViewers)
                .groupBy(qDictViewers.dict)
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
        List<String> bestDictIdList = redisService.getStringList(key);

        if (bestDictIdList == null) {
            redisService.setBestDict(key, getSafeBestDict());
            bestDictIdList = redisService.getStringList(key);

            if (bestDictIdList == null) {
                return dictRepository.findAllByOrderByViewsDesc(PageRequest.of(0, 5)).toList();
            }
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

    // Entity To Dto
    // DictDtoList to DictResponseDtoList
    private List<DictResponseDto> dictListToDictResponseDtoList(List<Tuple> dictTupleList, User user) {
        List<DictResponseDto> dictResponseDtoList = new ArrayList<>();

        // 1. dict 목록을 저장한다.
        // 2. dictLike 테이블에서 dict 목록을 IN 연산한 값을 가져온다.
        // 3. 이걸 HashMap 에 저장한다. 킷값으로. 조회할 때 시간복잡도가 O(1)
        // 4. 이 키값이 존재하는지 확인하는 식으로 비교한다.
        // 5. 성능 개선은 몰라도 N+1은 해결됨. ㄱㄱ
        // -> 100개의 데이터를 한꺼번에 호출한 결과 성능개선 효과 있음.

        // 좋아요 맵
        List<Long> dictIdList = getDictIdListByTupleList(dictTupleList);
        HashMap<String, Boolean> dictLikeMap = getDictLikeMap(dictIdList);

        for (Tuple tuple : dictTupleList) {
            Long dictId = tuple.get(0, Long.class);
            String title = tuple.get(1, String.class);
            String summary = tuple.get(2, String.class);
            String firstWriter = tuple.get(3, String.class);
            LocalDateTime createdAt = tuple.get(4, LocalDateTime.class);
            Integer likeCountInteger = tuple.get(5, Integer.class);
            int likeCount = likeCountInteger == null ? 0 : likeCountInteger;

            dictResponseDtoList.add(DictResponseDto.builder()
                    .dictId(dictId)
                    .title(title)
                    .summary(summary)
                    .firstWriter(firstWriter)
                    .createdAt(createdAt)
                    .isLike(user != null && dictLikeMap.get(dictId + ":" + user.getId()) != null)
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
        HashMap<String, Boolean> dictLikeMap = getDictLikeMap(dictIdList);

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
        HashMap<String, Boolean> dictLikeMap = getDictLikeMap(li);
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

    // endregion
}