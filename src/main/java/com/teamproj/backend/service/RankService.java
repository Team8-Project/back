package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.DictResponseDto;
import com.teamproj.backend.dto.rank.RankDictAllTimeResponseDto;
import com.teamproj.backend.dto.rank.RankResponseDto;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.QDict;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private final DictService dictService;
    private final DictRepository dictRepository;
    private final DictLikeRepository dictLikeRepository;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    private final JPAQueryFactory queryFactory;

    public List<RankResponseDto> getRank(int day) {
        // 받아오는 형식은 DTO 여야 함.
        return getUserRank(day);
    }

    public List<RankDictAllTimeResponseDto> getAllTimeDictRank(String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기 - 좋아요 했는지 여부 판단하기 위해 (select from user 시행 지점)
        User user = getSafeUserByUserDetails(userDetails);

        List<Dict> dictList = getAllTimeDictRankList();
        return dictListToRankDictAllTimeResponseDtoList(dictList, user);
    }

    // DictDtoList to DictResponseDtoList
    private List<RankDictAllTimeResponseDto> dictListToRankDictAllTimeResponseDtoList(List<Dict> dictList, User user) {
        List<RankDictAllTimeResponseDto> dictResponseDtoList = new ArrayList<>();

        // 작성자 맵
        HashMap<Long, String> firstWriterMap = dictService.getFirstWriterMap(dictList);
        // 좋아요 맵
        HashMap<String, Boolean> dictLikeMap = dictService.getDictLikeMap(dictList);
        // 좋아요 개수 맵
        HashMap<Long, Long> likeCountMap = dictService.getLikeCountMap(dictList);

        for (Dict dict : dictList) {
            // likeCountMap 에 값이 없을경우 좋아요가 없음 = 0개.
            int likeCount = likeCountMap.get(dict.getDictId()) == null ? 0 : likeCountMap.get(dict.getDictId()).intValue();

            dictResponseDtoList.add(RankDictAllTimeResponseDto.builder()
                    .dictId(dict.getDictId())
                    .title(dict.getDictName())
                    .summary(dict.getSummary())
                    .meaning(dict.getContent())
                    .firstWriter(firstWriterMap.get(dict.getDictId()))
                    .createdAt(dict.getCreatedAt())
                    .isLike(user != null && dictLikeMap.get(dict.getDictId() + ":" + user.getId()) != null)
                    .likeCount(likeCount)
                    .build());
        }

        return dictResponseDtoList;
    }

    // Get Safe Entity
    // 전체 기간 랭크
    private List<Dict> getAllTimeDictRankList() {
        QDict qDict = QDict.dict;
        return queryFactory
                .select(qDict)
                .from(qDict)
                .orderBy(qDict.dictLikeList.size().desc())
                .limit(10)
                .fetch();
    }

    // User By UserDetails
    private User getSafeUserByUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return null;
        }
        return jwtAuthenticateProcessor.getUser(userDetails);
    }

    private List<RankResponseDto> getUserRank(int day) {
        List<Tuple> tupleList = getUserRankTupleList(day);

        // 이후 DTO 에 담아서 return 해줘야 함.
        List<RankResponseDto> rankResponseDtoList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            Long userId = tuple.get(0, Long.class);
            String profileImage = tuple.get(1, String.class);
            String nickname = tuple.get(2, String.class);
            Long postCount = tuple.get(3, Long.class);
            rankResponseDtoList.add(RankResponseDto.builder()
                    .userId(userId)
                    .profileImage(profileImage)
                    .nickname(nickname)
                    .postCount(postCount)
                    .build());
        }

        return rankResponseDtoList;
    }

    private List<Tuple> getUserRankTupleList(int day) {
        QUser qUser = QUser.user;
        QDict qDict = QDict.dict;

        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.of(0, 0, 0)); //어제 00:00:00
        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)); //오늘 23:59:59

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory
                .select(qUser.id, qUser.profileImage, qUser.nickname, qDict.dictId.count().as(count)).distinct()
                .from(qDict)
                .join(qDict.firstAuthor, qUser)
                .where(qDict.createdAt.between(startDatetime, endDatetime))
                .orderBy(count.desc())
                .groupBy(qUser.id)
                .limit(10)
                .fetch();
    }
}
