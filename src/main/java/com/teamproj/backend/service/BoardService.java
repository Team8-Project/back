package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.board.*;
import com.teamproj.backend.dto.board.BoardDelete.BoardDeleteResponseDto;
import com.teamproj.backend.dto.board.BoardDetail.BoardDetailResponseDto;
import com.teamproj.backend.dto.board.BoardHashTag.BoardHashTagResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardLikeResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardYesterdayLikeCountRankDto;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardSearch.BoardSearchResponseDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateResponseDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadResponseDto;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.board.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_CATEGORY;


@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardImageRepository boardImageRepository;
    private final BoardHashTagRepository boardHashTagRepository;
    private final BoardViewersRepository boardViewersRepository;
    private final BoardTodayLikeRepository boardTodayLikeRepository;

    private final CommentService commentService;
    private final RedisService redisService;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final EntityManager entityManager;
    private final S3Uploader s3Uploader;

    private final JPAQueryFactory queryFactory;

    private final String S3dirName = "boardImages";

    //region 게시글 전체조회
    public List<BoardResponseDto> getBoard(String categoryName, int page, int size) {
        Optional<BoardCategory> boardCategory = boardCategoryRepository.findById(categoryName.toUpperCase());
        if (!boardCategory.isPresent()) {
            throw new NullPointerException(ExceptionMessages.NOT_EXIST_CATEGORY);
        }

        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "boardId");
        Pageable pageable = PageRequest.of(page, size, sort);

        Optional<Page<Board>> boardList = boardRepository.findAllByBoardCategoryAndEnabled(boardCategory.get(), true, pageable);
        return boardList.map(this::boardListToBoardResponseDtoList).orElseGet(ArrayList::new);
    }

    private List<BoardResponseDto> boardListToBoardResponseDtoList(Page<Board> boardList) {
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        for (Board board : boardList) {
            boardResponseDtoList.add(BoardResponseDto.builder()
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
                    .hashTags(board.getBoardHashTagList().stream().map(
                            e -> e.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                    )
                    .build());
        }

        return boardResponseDtoList;
    }
    //endregion

    //region 게시글 작성
    public BoardUploadResponseDto uploadBoard(UserDetailsImpl userDetails,
                                              BoardUploadRequestDto boardUploadRequestDto,
                                              String categoryName,
                                              MultipartFile multipartFile) throws IOException {

        String boardTitle = boardUploadRequestDto.getTitle();
        String boardContent = boardUploadRequestDto.getContent();
        List<String> boardRequestHashTagList = boardUploadRequestDto.getHashTags();

        if (boardTitle.isEmpty()) {
            throw new IllegalArgumentException(ExceptionMessages.TITLE_IS_EMPTY);
        }
        if (boardContent.isEmpty()) {
            throw new IllegalArgumentException(ExceptionMessages.CONTENT_IS_EMPTY);
        }

        if (boardRequestHashTagList != null && boardRequestHashTagList.size() > 5) {
            throw new IllegalArgumentException(ExceptionMessages.HASHTAG_MAX_FIVE);
        }

        BoardCategory boardCategory = boardCategoryRepository.findById(categoryName.toUpperCase())
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_CATEGORY)
                );

        String imageUrl = "";
        if (multipartFile != null) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
        }


        Board board = Board.builder()
                .title(boardTitle)
                .content(boardContent)
                .boardCategory(boardCategory)
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .thumbNail(imageUrl)
                .enabled(true)
                .build();
        boardRepository.save(board);


        List<BoardHashTag> boardHashTagList = new ArrayList<>();
        if (boardRequestHashTagList != null) {
            for (String hashTag : boardRequestHashTagList) {
                BoardHashTag boardHashTag = BoardHashTag.builder()
                        .hashTagName(hashTag)
                        .board(board)
                        .build();

                boardHashTagList.add(boardHashTag);
            }

            boardHashTagRepository.saveAll(boardHashTagList);
            board.setHashTagList(boardHashTagList);
            boardRepository.save(board);
        }


        BoardImage boardImage = BoardImage.builder()
                .board(board)
                .imageUrl(imageUrl)
                .build();

        boardImageRepository.save(boardImage);


        return BoardUploadResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getBoardCategory().getCategoryName())
                .thumbNail(board.getThumbNail())
                .createdAt(board.getCreatedAt() == null ? null : board.getCreatedAt())
                .hashTags(boardHashTagList.size() == 0 ? null : boardHashTagList.stream().map(
                        e -> e.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                )
                .build();
    }
    //endregion

    //region 게시글 상세 조회
    public BoardDetailResponseDto getBoardDetail(Long boardId, String token) {
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_BOARD)
                );

        boolean isLike = false;
        if (userDetails != null) {
            Optional<BoardLike> boardLike = boardLikeRepository.findByBoardAndUser(
                    board, jwtAuthenticateProcessor.getUser(userDetails)
            );

            if (boardLike.isPresent()) {
                isLike = true;
            }
        }

        if (isView(board)) {
            boardViewersRepository.save(BoardViewers.builder()
                    .viewerIp(StatisticsUtils.getClientIp())
                    .board(board)
                    .build());
            boardRepository.updateView(boardId);
        }

        List<BoardLike> boardLikeList = boardLikeRepository.findAllByBoard(board);

        return BoardDetailResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getNickname())
                .profileImageUrl(board.getUser().getProfileImage())
                .thumbNail(board.getThumbNail())
                .createdAt(board.getCreatedAt())
                .views(board.getViews())
                .likeCnt(boardLikeList.size())
                .isLike(isLike)
                .commentList(commentService.getCommentList(board))
                .build();
    }

    private boolean isView(Board board) {
        Optional<BoardViewers> boardViewers = boardViewersRepository.findByViewerIpAndBoard(StatisticsUtils.getClientIp(), board);
        return !boardViewers.isPresent();
    }
    //endregion

    //region 게시글 업데이트(수정)
    @Transactional
    public BoardUpdateResponseDto updateBoard(Long boardId, UserDetailsImpl userDetails,
                                              BoardUpdateRequestDto boardUpdateRequestDto,
                                              MultipartFile multipartFile) throws IOException {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_BOARD)
                );
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException(ExceptionMessages.NOT_MY_BOARD);
        }


        List<String> inputHashTagStrList = boardUpdateRequestDto.getHashTags();
        if (inputHashTagStrList.size() > 5) {
            throw new IllegalArgumentException(ExceptionMessages.HASHTAG_MAX_FIVE);
        }

        boardHashTagRepository.deleteAllByIdInQuery(board);


        List<BoardHashTag> boardHashTagList = new ArrayList<>();

        for (String tempStr : inputHashTagStrList) {
            BoardHashTag boardHashTag = BoardHashTag.builder()
                    .hashTagName(tempStr)
                    .board(board)
                    .build();
            boardHashTagList.add(boardHashTag);
        }

        boardHashTagRepository.saveAll(boardHashTagList);


        String imageUrl = "";
        if (!(multipartFile.getSize() == 0)) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
            String oldImageUrl = URLDecoder.decode(
                    board.getThumbNail().replace(
                            "https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/", ""
                    ),
                    "UTF-8"
            );

            s3Uploader.deleteFromS3(oldImageUrl);
        }

        board.update(boardUpdateRequestDto, imageUrl);

        boardRepository.save(board);

        return BoardUpdateResponseDto.builder()
                .result("게시글 수정 완료")
                .build();
    }
    //endregion

    //region 게시글 삭제
    public BoardDeleteResponseDto deleteBoard(UserDetailsImpl userDetails, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_BOARD)
                );

        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException(ExceptionMessages.NOT_MY_BOARD);
        }

        board.setEnabled(false);
        boardRepository.save(board);

        return BoardDeleteResponseDto.builder()
                .result("게시글 삭제 완료")
                .build();
    }
    //endregion

    //region 게시글 좋아요
    public BoardLikeResponseDto boardLike(UserDetailsImpl userDetails, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_BOARD)
                );

        Optional<BoardLike> findBoardLike = boardLikeRepository.findByBoardAndUser(
                board, jwtAuthenticateProcessor.getUser(userDetails)
        );

        if (findBoardLike.isPresent()) {
            boardLikeRepository.delete(findBoardLike.get());

            todayLikeCancelProc(board);

            return BoardLikeResponseDto.builder()
                    .result(false)
                    .build();
        }

        BoardLike boardLike = BoardLike.builder()
                .board(board)
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .build();

        boardLikeRepository.save(boardLike);

        todayLikeProc(board, board.getBoardCategory());

        return BoardLikeResponseDto.builder()
                .result(true)
                .build();
    }

    private void todayLikeCancelProc(Board board) {
        Optional<BoardTodayLike> boardTodayLike = boardTodayLikeRepository.findByBoard(board);
        if (boardTodayLike.isPresent()) {
            Long likeCount = boardTodayLike.get().getLikeCount();
            if (likeCount > 0) {
                boardTodayLike.get().setLikeCount(likeCount - 1);
                boardTodayLikeRepository.save(boardTodayLike.get());
            }
        }
    }

    private void todayLikeProc(Board board, BoardCategory boardCategory) {
        Optional<BoardTodayLike> boardTodayLike = boardTodayLikeRepository.findByBoard(board);
        if (boardTodayLike.isPresent()) {
            boardTodayLike.get().setLikeCount(boardTodayLike.get().getLikeCount() + 1);
            boardTodayLikeRepository.save(boardTodayLike.get());
        } else {
            BoardTodayLike newBoardTodayLike = BoardTodayLike.builder()
                    .board(board)
                    .boardCategory(boardCategory)
                    .likeCount(1L)
                    .build();
            boardTodayLikeRepository.save(newBoardTodayLike);
        }
    }
    //endregion

    //region 게시글 검색
    public List<BoardSearchResponseDto> boardSearch(String q) {
        if (q == null || q.isEmpty()) {
            throw new NullPointerException(ExceptionMessages.SEARCH_IS_EMPTY);
        }

//        RecentSearch recentSearch = RecentSearch.builder()
//                .viewerIp(StatisticsUtils.getClientIp())
//                .query(q)
//                .type(QueryTypeEnum.BOARD)
//                .build();
//        recentSearchRepository.save(recentSearch);

        Optional<List<Board>> findBoardList = boardRepository.findByTitleContaining(q);

        List<Board> boardList = findBoardList.get();
        if (boardList.size() == 0) {
            throw new NullPointerException(ExceptionMessages.SEARCH_BOARD_IS_EMPTY);
        }


        List<BoardSearchResponseDto> boardSearchResponseDtoList = new ArrayList<>();
        for (Board board : boardList) {
            boardSearchResponseDtoList.add(
                    BoardSearchResponseDto.builder()
                            .boardId(board.getBoardId())
                            .thumbNail(board.getThumbNail())
                            .title(board.getTitle())
                            .username(board.getUser().getUsername())
                            .writer(board.getUser().getNickname())
                            .content(board.getContent())
                            .createdAt(board.getCreatedAt())
                            .views(board.getViews())
                            .likeCnt(board.getLikes().size())
                            .hashTags(board.getBoardHashTagList().size() == 0 ? null : board.getBoardHashTagList().stream().map(
                                    h -> h.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                            )
                            .build()
            );
        }

        return boardSearchResponseDtoList;
    }
    //endregion

    //region 해시태그 추천
    public BoardHashTagResponseDto getRecommendHashTag() {
        List<String> recommendHashTagStrList = redisService.getRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY);

        List<String> resultdHashTagStrList = new ArrayList<>();
        if (recommendHashTagStrList == null) {
            JPAQuery<BoardHashTag> query = new JPAQuery<>(entityManager, MySqlJpaTemplates.DEFAULT);
            QBoardHashTag qBoardHashTag = new QBoardHashTag("boardHashTag");

            List<BoardHashTag> boardHashTagList = query.from(qBoardHashTag)
                    .orderBy(NumberExpression.random().asc())
                    .limit(7)
                    .fetch();

            redisService.setRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY, boardHashTagList);
            resultdHashTagStrList = redisService.getRecommendHashTag(RedisKey.HASHTAG_RECOMMEND_KEY);
        }

        return BoardHashTagResponseDto.builder()
                .hashTags(resultdHashTagStrList)
                .build();
    }
    //endregion

    // region 인기 게시글
    public List<MainTodayBoardResponseDto> getTodayBoard(int count) {
        List<BoardYesterdayLikeCountRankDto> boardYesterdayLikeCountRankDtoList = getTodayBoardElement(count, "FREEBOARD");

        // BoardYesterdayLikeCountRankDto To MainTodayBoardResponseDto
        List<MainTodayBoardResponseDto> mainTodayBoardResponseDtoList = new ArrayList<>();
        for (BoardYesterdayLikeCountRankDto dto : boardYesterdayLikeCountRankDtoList) {
            mainTodayBoardResponseDtoList.add(MainTodayBoardResponseDto.builder()
                    .boardId(dto.getBoardId())
                    .thumbNail(dto.getThumbNail())
                    .title(dto.getTitle())
                    .writer(dto.getNickname())
                    .build());
        }
        return mainTodayBoardResponseDtoList;
    }
    // endregion

    // region 명예의 전당
    public List<MainMemeImageResponseDto> getTodayImage(int count) {
        List<BoardYesterdayLikeCountRankDto> boardYesterdayLikeCountRankDtoList = getTodayBoardElement(count, "IMAGEBOARD");

        // BoardYesterdayLikeCountRankDto To MainMemeImageResponseDto
        List<MainMemeImageResponseDto> mainMemeImageResponseDtoList = new ArrayList<>();
        for (BoardYesterdayLikeCountRankDto dto : boardYesterdayLikeCountRankDtoList) {
            mainMemeImageResponseDtoList.add(MainMemeImageResponseDto.builder()
                    .boardId(dto.getBoardId())
                    .imageUrl(dto.getThumbNail())
                    .build());
        }

        return mainMemeImageResponseDtoList;
    }
    // endregion

    // region 인기 게시글, 명예의 전당 어제 좋아요 데이터 산출 도구
    private List<BoardYesterdayLikeCountRankDto> getTodayBoardElement(int count, String category) {
        BoardCategory boardCategory = getSafeBoardCategory(category);
        List<Tuple> tupleList = getYesterdayLikeCountRankTuple(boardCategory, count);

        List<BoardYesterdayLikeCountRankDto> result = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            result.add(BoardYesterdayLikeCountRankDto.builder()
                    .boardId(tuple.get(0, Long.class))
                    .thumbNail(tuple.get(1, String.class))
                    .title(tuple.get(2, String.class))
                    .nickname(tuple.get(3, String.class))
                    .likeCnt(tuple.get(4, Long.class))
                    .build());
        }

        return result;
    }

    private List<Tuple> getYesterdayLikeCountRankTuple(BoardCategory boardCategory, int count) {
        QBoardLike qBoardLike = QBoardLike.boardLike;
        QBoard qBoard = QBoard.board;
        QUser qUser = QUser.user;

        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0)); //어제 00:00:00
        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)); //오늘 23:59:59
        NumberPath<Long> likeCnt = Expressions.numberPath(Long.class, "c");

        return queryFactory.select(qBoard.boardId, qBoard.thumbNail, qBoard.title, qUser.nickname, qBoardLike.board.count().as(likeCnt))
                .from(qBoardLike)
                .leftJoin(qBoardLike.board, qBoard)
                .leftJoin(qBoardLike.user, qUser)
                .where(qBoard.boardCategory.eq(boardCategory)
                        .and(qBoardLike.createdAt.between(startDatetime, endDatetime)))
                .groupBy(qBoardLike.board)
                .orderBy(likeCnt.desc())
                .limit(count)
                .fetch();
    }

    private BoardCategory getSafeBoardCategory(String category) {
        return boardCategoryRepository.findById(category).orElseThrow(
                () -> new NullPointerException(NOT_EXIST_CATEGORY)
        );
    }
    // endregion
}
