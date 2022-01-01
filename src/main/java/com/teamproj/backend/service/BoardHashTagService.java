package com.teamproj.backend.service;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.teamproj.backend.dto.board.BoardHashTag.BoardHashTagResponseDto;
import com.teamproj.backend.model.board.BoardHashTag;
import com.teamproj.backend.model.board.QBoardHashTag;
import com.teamproj.backend.util.MySqlJpaTemplates;
import com.teamproj.backend.util.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardHashTagService {
    private final RedisService redisService;

    private final EntityManager entityManager;

    //region 해시태그 추천
    public BoardHashTagResponseDto getRecommendHashTag() {
        List<String> recommendHashTagStrList = redisService.getRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY);

        List<String> resultHashTagStrList = new ArrayList<>();
        if (recommendHashTagStrList == null) {
            JPAQuery<BoardHashTag> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
            QBoardHashTag qBoardHashTag = new QBoardHashTag("boardHashTag");

            List<BoardHashTag> boardHashTagList = query.from(qBoardHashTag)
                    .orderBy(NumberExpression.random().asc())
                    .limit(20)
                    .fetch();

            redisService.setRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY, boardHashTagList);
            resultHashTagStrList = redisService.getRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY);
        }

        Collections.shuffle(recommendHashTagStrList);
        return BoardHashTagResponseDto.builder()
                .hashTags(resultHashTagStrList.subList(0, 6))
                .build();
    }
    //endregion
}