package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictLike;
import com.teamproj.backend.model.dict.QDict;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private final DictRepository dictRepository;
    private final DictLikeRepository dictLikeRepository;

    private final JPAQueryFactory queryFactory;

    public void getRankOfWeek() {
        // 받아오는 형식은 DTO 여야 함.
        List<User> userList = getUserRank(7);
    }

    public void getRankOfMonth() {
        List<User> userList = getUserRank(30);
    }

    public void getAllTimeDictRank() {
        List<Dict> dictList = getAllTimeDictRankList();
        // DTO 로 포장하면 됨
    }

    private List<Dict> getAllTimeDictRankList() {
        QDict qDict = QDict.dict;
        return queryFactory
                .select(qDict)
                .from(qDict)
                .orderBy(qDict.dictLikeList.size().desc())
                .limit(10)
                .fetch();
    }

    private List<User> getUserRank(int day) {
        List<Tuple> tupleList = getUserRankTupleList(day);

        // 이후 DTO 에 담아서 return 해줘야 함.
        for (Tuple tuple : tupleList) {
            Long userId = tuple.get(0, Long.class);
            String profileImage = tuple.get(1, String.class);
            String nickname = tuple.get(2, String.class);
            Long dictCnt = tuple.get(3, Long.class);
        }

        return new ArrayList<>();
    }

    private List<Tuple> getUserRankTupleList(int day) {
        QUser qUser = QUser.user;
        QDict qDict = QDict.dict;

        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now().minusDays(day), LocalTime.of(0, 0, 0)); //어제 00:00:00
        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)); //오늘 23:59:59

        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        return queryFactory
                .select(qUser.id, qUser.profileImage, qUser.nickname, qDict.dictId.count().as(count))
                .from(qUser)
                .join(qDict.firstAuthor, qUser)
                .where(qDict.createdAt.between(startDatetime, endDatetime))
                .orderBy(count.desc())
                .groupBy(qUser.id)
                .fetch();
    }
}
