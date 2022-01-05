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
import java.util.Collections;
import java.util.List;

import static com.teamproj.backend.util.RedisKey.*;

@Service
@RequiredArgsConstructor
public class BoardHashTagService {
    private final RedisService redisService;
    private final EntityManager entityManager;
    //region 해시태그 추천
    public BoardHashTagResponseDto getRecommendHashTag() {
        List<String> recommendHashTagStrList = redisService.getRecommendHashTag(HASHTAG_RECOMMEND_KEY);

        if (recommendHashTagStrList.size() == 0) {
            JPAQuery<BoardHashTag> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
            QBoardHashTag qBoardHashTag = new QBoardHashTag("boardHashTag");

            List<BoardHashTag> boardHashTagList = query.from(qBoardHashTag)
                    .orderBy(NumberExpression.random().asc())
                    .limit(20)
                    .fetch();
            redisService.setRecommendHashTag(HASHTAG_RECOMMEND_KEY, boardHashTagList);
            recommendHashTagStrList = redisService.getRecommendHashTag(HASHTAG_RECOMMEND_KEY);
        }

        int returnSize = Math.min(recommendHashTagStrList.size(), 6);

        Collections.shuffle(recommendHashTagStrList);
        return BoardHashTagResponseDto.builder()
                .hashTags(recommendHashTagStrList.subList(0, returnSize))
                .build();
    }
    //endregion
}