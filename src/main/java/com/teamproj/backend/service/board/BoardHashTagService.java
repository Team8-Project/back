package com.teamproj.backend.service.board;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.teamproj.backend.Repository.board.BoardHashTagRepository;
import com.teamproj.backend.dto.BoardHashTag.BoardHashTagResponseDto;
import com.teamproj.backend.dto.BoardHashTag.BoardHashTagSearchResponseDto;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardHashTag;
import com.teamproj.backend.model.board.QBoardHashTag;
import com.teamproj.backend.service.CommentService;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.util.MySqlJpaTemplates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.teamproj.backend.exception.ExceptionMessages.SEARCH_IS_EMPTY;
import static com.teamproj.backend.util.RedisKey.HASHTAG_RECOMMEND_KEY;

@Service
@RequiredArgsConstructor
public class BoardHashTagService {
    private final BoardHashTagRepository boardHashTagRepository;

    private final CommentService commentService;

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

    //region 해시태그 검색
    public List<BoardHashTagSearchResponseDto> getBoardHashTagSearch(String q) {
        // 1. 검색어 빈 값 체크
        if (q == null || q.isEmpty()) {
            throw new NullPointerException(SEARCH_IS_EMPTY);
        }
        // 2. 해시태그 이름으로 게시글 조회
        List<BoardHashTag> boardHashTagList = boardHashTagRepository.findByHashTagName(q);
        // 3. 조회한 해시태그가 등록된 게시글 중복 및 삭제 안된것 게시글 HashSet에 add
        HashSet<Board> boards = new HashSet<>();
        for(BoardHashTag boardHashTag : boardHashTagList) {
            Board board = boardHashTag.getBoard();
            if(board.isEnabled() == true)
                boards.add(board);
        }
        // 4. 게시글 정보 Response
        List<BoardHashTagSearchResponseDto> boardHashTagSearchResponseDtoList = new ArrayList<>();
        for(Board board : boards) {
            boardHashTagSearchResponseDtoList.add(
                    BoardHashTagSearchResponseDto.builder()
                            .boardId(board.getBoardId())
                            .thumbNail(board.getThumbNail())
                            .title(board.getTitle())
                            .username(board.getUser().getUsername())
                            .profileImageUrl(board.getUser().getProfileImage())
                            .writer(board.getUser().getNickname())
                            .content(board.getContent())
                            .createdAt(board.getCreatedAt())
                            .views(board.getViews())
                            .likeCnt(board.getLikes().size())
                            .commentCnt(commentService.getCommentList(board).size())
                            .hashTags(board.getBoardHashTagList().size() == 0 ? null : board.getBoardHashTagList().stream().map(
                                    h -> h.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                            )
                            .build()
            );
        }
        return boardHashTagSearchResponseDtoList;
    }
    //endregion
}
