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
        // 1. 추천 해시태그 레디스에서 캐싱 데이터 가져오기
        List<String> recommendHashTagStrList = redisService.getRecommendHashTag(HASHTAG_RECOMMEND_KEY);
        // 2. 레디스에서 가져온 데이터가 null 이거나 limit 20개보다 크면
        if (recommendHashTagStrList == null || recommendHashTagStrList.size() < 20) {
            // QueryDSL 게시판 해시태그
            JPAQuery<BoardHashTag> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
            QBoardHashTag qBoardHashTag = new QBoardHashTag("boardHashTag");
            // 게시판 해시태그 테이블에서 임의에 해시태그 20개 가져오기
            List<BoardHashTag> boardHashTagList = query.from(qBoardHashTag)
                    .orderBy(NumberExpression.random().asc())
                    .limit(20)
                    .fetch();
            // 레디스에 해당 데이터 set
            redisService.setRecommendHashTag(HASHTAG_RECOMMEND_KEY, boardHashTagList);
            // 저장된 레디스 데이터 가져오기
            recommendHashTagStrList = redisService.getRecommendHashTag(HASHTAG_RECOMMEND_KEY);
        }
        // 3. 레디스에서 가져온 해시태그 추천 데이터 최대 범위 선정
        // - 가져온 데이터가 0이면 최소 값인 0을 retrunSize에 할당
        int returnSize = Math.min(recommendHashTagStrList.size(), 6);

        // 4. 레디스에서 가져온 20개의 데이터를 섞는다.
        // - 데이터가 랜덤하게 가져온 걸로 보이기 위해
        Collections.shuffle(recommendHashTagStrList);
        // 5. 레디스에서 가져온 20개의 데이터 중 0 부터 returnSize만큼 잘라서 Response
        return BoardHashTagResponseDto.builder()
                .hashTags(recommendHashTagStrList.subList(0, returnSize))
                .build();
    }
    //endregion
}