package com.teamproj.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.board.*;
import com.teamproj.backend.dto.board.BoardDelete.BoardDeleteResponseDto;
import com.teamproj.backend.dto.board.BoardDetail.BoardDetailResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardLikeResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardYesterdayLikeCountRankDto;
import com.teamproj.backend.dto.board.BoardMemeBest.BoardMemeBestResponseDto;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardSearch.BoardSearchResponseDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateResponseDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadResponseDto;
import com.teamproj.backend.dto.comment.CommentResponseDto;
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.S3Uploader;
import com.teamproj.backend.util.StatisticsUtils;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.model.board.QBoardLike.boardLike;
import static com.teamproj.backend.util.RedisKey.*;

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
    private final S3Uploader s3Uploader;

    private final JPAQueryFactory queryFactory;

    private final String S3dirName = "boardImages";

    //region 게시글 전체조회
    public List<BoardResponseDto> getBoard(String categoryName, int page, int size, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. Request로 넘어온 카테고리 네임 DB에서 조회
        BoardCategory boardCategory = getSafeBoardCategory(categoryName);

        // 3. RequestParam으로 넘어온 페이지 값과 사이즈 값 바탕으로 페이지네이션(내림차순 DESC)
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "boardId");
        Pageable pageable = PageRequest.of(page, size, sort);

        // 4. 카테고리와 enabled(삭제 안된) 데이터를 페이지네이션 조건에 맞게 리스트형식으로 가져오기
        Page<Board> boardList = boardRepository.findAllByBoardCategoryAndEnabled(boardCategory, true, pageable)
                .orElseThrow(
                        () -> new NullPointerException(BOARD_IS_EMPTY)
                );

        return getBoardResponseDtoList(userDetails, boardList);
    }

    private List<BoardResponseDto> getBoardResponseDtoList(UserDetailsImpl userDetails, Page<Board> boardList) {
        // 5. DB에서 받아온 게시글 List 데이터를 담을 Response Dto 생성
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        for(Board board : boardList) {
            // 6. 게시글 좋아요 여부 확인(로그인한 유저만)
            boolean isLike = false;
            if (userDetails != null) {
                Optional<BoardLike> boardLike = boardLikeRepository.findByBoardAndUser(
                        board, jwtAuthenticateProcessor.getUser(userDetails)
                );

                if (boardLike.isPresent()) {
                    isLike = true;
                }
            }

            // 7. 게시글 List 데이터를 Dto에 List에 담아서 리턴
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
                    .isLike(isLike)
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
        // 글 작성할려는 유저 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);
        // 1. Request로 넘어온 데이터 유효성 검사(게시글 제목, 게시글 내용)
        String boardTitle = boardUploadRequestDto.getTitle();
        String boardContent = boardUploadRequestDto.getContent();
        if (boardTitle.isEmpty()) {
            throw new IllegalArgumentException(TITLE_IS_EMPTY);
        }
        if (boardContent.isEmpty()) {
            throw new IllegalArgumentException(CONTENT_IS_EMPTY);
        }

        // 2. 게시글에 해당하는 해시태그들 List 형식에 할당
        List<String> boardRequestHashTagList = boardUploadRequestDto.getHashTags();
        // 3. 입력된 해시태그가 5개 넘는지 체크
        HashTagIsMaxFiveCheck(boardRequestHashTagList);
        // 4. Request로 넘어온 카테고리 네임 DB에서 조회
        BoardCategory boardCategory = getSafeBoardCategory(categoryName);
        // 5. multipartFile로 넘어온 이미지 데이터 null 체크 => null이 아니면 S3 버킷에 저장
        String imageUrl = "";
        if (multipartFile != null) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
        }

        // 6. 게시글 데이터 DB에 저장
        Board board = Board.builder()
                .title(boardTitle)                                    // 제목
                .content(boardContent)                                // 내용
                .boardCategory(boardCategory)                         // 카테고리
                .user(jwtAuthenticateProcessor.getUser(userDetails))  // 유저
                .thumbNail(imageUrl)                                  // 이미지URL
                .enabled(true)                                        // 게시글 삭제 여부
                .build();
        boardRepository.save(board);

        // 7. Request로 넘어온 해시태그가 null이 아니면 해시태그 데이터 저장
        if (boardRequestHashTagList != null) {
            for (String hashTag : boardRequestHashTagList) {
                BoardHashTag boardHashTag = BoardHashTag.builder()
                        .hashTagName(hashTag)
                        .board(board)
                        .build();

                board.getBoardHashTagList().add(boardHashTag);
            }
            boardHashTagRepository.saveAll(board.getBoardHashTagList());
        }

        // 8. 작성한 게시글에 맞는 이미지 저장
        BoardImage boardImage = BoardImage.builder()
                .board(board)
                .imageUrl(imageUrl)
                .build();
        boardImageRepository.save(boardImage);

        // 9. 저장한 데이터 기반으로 Response(게시글번호, 제목, 내용, 카테고리, 이미지URL, 생성날짜, 해시태그 리스트)
        return BoardUploadResponseDto.builder()
                .boardId(board.getBoardId())                                            // 게시글아이디
                .title(board.getTitle())                                                // 제목
                .content(board.getContent())                                            // 내용
                .category(board.getBoardCategory().getCategoryName())                   // 카테고리
                .thumbNail(board.getThumbNail())                                        // 이미지URL
                .createdAt(board.getCreatedAt() == null ? null : board.getCreatedAt())  // 게시글 생성 날짜
                .hashTags(board.getBoardHashTagList().size() == 0 ? null : board.getBoardHashTagList().stream().map(
                        e -> e.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                )                                                                       // 게시글 해시태그 리스트
                .build();
    }
    //endregion

    //region 게시글 상세 조회
    public BoardDetailResponseDto getBoardDetail(Long boardId, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 게시글 조회
        Board board = getSafeBoard(boardId);

        // 3. 게시글 좋아요 여부 조회(로그인한 유저만)
        boolean isLike = false;
        if (userDetails != null) {
            Optional<BoardLike> boardLike = boardLikeRepository.findByBoardAndUser(
                    board, jwtAuthenticateProcessor.getUser(userDetails)
            );

            if (boardLike.isPresent()) {
                isLike = true;
            }
        }

        // 4. 게시글 조회수 관련 처리 로직
        // - 조회하는 유저 IP를 통해 조회수 새로고침과 같은 중복 처리 방지
        if (isView(board)) {
            boardViewersRepository.save(BoardViewers.builder()
                    .viewerIp(StatisticsUtils.getClientIp())
                    .board(board)
                    .build());
            boardRepository.updateView(boardId);
        }

        // 5. 게시글 좋아요 리스트 조회
        List<BoardLike> boardLikeList = boardLikeRepository.findAllByBoard(board);

        List<CommentResponseDto> commentList = commentService.getCommentList(board);

        // 6. 조회한 게시글 Response 전송
        // (게시글 아이디, 제목, 작성자 아이디, 내용, 작성자 닉네임,
        // 게시글 작성자 프로필Img, 이미지URL, 생성날짜, 조회수, 좋아요 수, 좋아요)
        return BoardDetailResponseDto.builder()
                .boardId(board.getBoardId())                            // 게시글 아이디
                .title(board.getTitle())                                // 제목
                .username(board.getUser().getUsername())                // 게시글 작성유저 아이디
                .content(board.getContent())                            // 내용
                .writer(board.getUser().getNickname())                  // 게시글 작성유저 닉네임
                .profileImageUrl(board.getUser().getProfileImage())     // 게시글 작성유저 프로필이미지
                .thumbNail(board.getThumbNail())                        // 게시글 이미지URL
                .createdAt(board.getCreatedAt())                        // 생성날짜
                .views(board.getViews())                                // 조회수
                .likeCnt(boardLikeList.size())                          // 좋아요수
                .isLike(isLike)                                         // 로그인한 유저 게시글 좋아요 여부
                .commentList(commentList)                               // 댓글 리스트
                .commentCnt(commentList.size())                         // 댓글 갯수
                .hashTags(board.getBoardHashTagList().size() == 0 ? null : board.getBoardHashTagList().stream().map(
                        h -> h.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
                )                                                       // 게시글 해시태그 리스트
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
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 업데이트할 게시글 조회
        Board board = getSafeBoard(boardId);
        // 2. 게시글 수정 권한 체크
        checkPermissionToBoard(userDetails, board);
        // 3. 수정할 해시태그 Request
        List<String> inputHashTagStrList = boardUpdateRequestDto.getHashTags();
        // 4. 입력된 해시태그가 5개 넘는지 체크
        HashTagIsMaxFiveCheck(inputHashTagStrList);

        // 5. 개시글의 기존 해시태그 리스트 삭제 후 새로운 해시태그 저장
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

        // 6. multipartFile로 넘어온 이미지 파일 저장
        // - 기존에 S3에 저장되어 있는 이미지 삭제 후 저장
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

        // 7. 게시글 저장 및 Response 전송
        boardRepository.save(board);
        return BoardUpdateResponseDto.builder()
                .result("게시글 수정 완료")
                .build();
    }
    //endregion

    //region 게시글 삭제
    public BoardDeleteResponseDto deleteBoard(UserDetailsImpl userDetails, Long boardId) {
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 삭제할 게시글 조회
        Board board = getSafeBoard(boardId);
        // 2. 게시글 삭제 권한 체크
        checkPermissionToBoard(userDetails, board);
        // 3. 게시글 삭제 => enabled = false
        board.setEnabled(false);
        boardRepository.save(board);

        return BoardDeleteResponseDto.builder()
                .result("게시글 삭제 완료")
                .build();
    }


    //endregion

    //region 게시글 좋아요
    public BoardLikeResponseDto boardLike(UserDetailsImpl userDetails, Long boardId) {
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 좋아요할 게시글 조회
        Board board = getSafeBoard(boardId);
        // 2. 해당 게시글 좋아요 조회
        Optional<BoardLike> findBoardLike = boardLikeRepository.findByBoardAndUser(
                board, jwtAuthenticateProcessor.getUser(userDetails)
        );
        // 3. 게시글 좋아요 여부 확인
        // - 게시글 좋아요 되어있다면 해당 게시글 좋아요 삭제
        // - 게시판 오늘의 좋아요 취소
        // - 결과 값 false로 Response
        if (findBoardLike.isPresent()) {
            boardLikeRepository.delete(findBoardLike.get());
            todayLikeCancelProc(board);

            return BoardLikeResponseDto.builder()
                    .result(false)
                    .build();
        }
        // 4. 게시글 좋아요 저장
        BoardLike boardLike = BoardLike.builder()
                .board(board)
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .build();
        boardLikeRepository.save(boardLike);

        // 5. 게시판 오늘의 좋아요 카운트 + 1
        todayLikeProc(board, board.getBoardCategory());
        // 6. 결과값 true로 Response
        return BoardLikeResponseDto.builder()
                .result(true)
                .build();
    }
    // 게시판 오늘의 좋아요 카운트 - 1
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
    // 게시판 오늘의 좋아요 카운트 + 1
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
        // 1. 검색어 입력값이 empty 혹은 null이면 Exception 발생
        if (q == null || q.isEmpty()) {
            throw new NullPointerException(SEARCH_IS_EMPTY);
        }

//        RecentSearch recentSearch = RecentSearch.builder()
//                .viewerIp(StatisticsUtils.getClientIp())
//                .query(q)
//                .type(QueryTypeEnum.BOARD)
//                .build();
//        recentSearchRepository.save(recentSearch);
        // 2. 제목에 검색어가 포함되어 있는 게시글 리스트 조회
        Optional<List<Board>> findBoardList = boardRepository.findByTitleContaining(q);
        // 3. 게시글 검색 결과가 없으면 빈 ArrayList 리턴
        List<Board> boardList = findBoardList.get();
        if (boardList.size() == 0) {
            return new ArrayList<>();
        }

        // 3. 검색 결과가 있으면 해당 게시글들 Response
        List<BoardSearchResponseDto> boardSearchResponseDtoList = new ArrayList<>();
        for (Board board : boardList) {
            boardSearchResponseDtoList.add(
                    BoardSearchResponseDto.builder()
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

        return boardSearchResponseDtoList;
    }
    //endregion

    // region 인기 게시글
    public List<MainTodayBoardResponseDto> getTodayBoard(int count) {
        // 1. 인기 게시글, 명예의 전당 어제 좋아요 데이터 산출 도구 데이터
        List<BoardYesterdayLikeCountRankDto> boardYesterdayLikeCountRankDtoList = getTodayBoardElement(count, "FREEBOARD");
        // 2. 인기 게시글, 명예의 전당 어제 좋아요 데이터 산출 도구 데이터 Dto에 담아 Response
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
        QBoardLike qBoardLike = boardLike;
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
                        .and(qBoardLike.createdAt.between(startDatetime, endDatetime))
                        .and(qBoard.enabled.eq(true)))
                .groupBy(qBoardLike.board)
                .orderBy(likeCnt.desc())
                .limit(count)
                .fetch();
    }

    // endregion

    //region 명예의 밈짤 받기
    public List<BoardMemeBestResponseDto> getBestMemeImg(String categoryName, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 레디스 캐싱데이터에 명예의 밈짤 데이터 가져오기
        List<BoardMemeBestResponseDto> boardMemeBestResponseDtoList = redisService.getBestMemeImgList(BEST_MEME_JJAL_KEY);
        // 3. 레디스에 명예의 밈짤 데이터가 null이면
        // - 밈 게시글(Image Board)에서 3개의 명예의 밈짤 가져오는 함수 실행
        // - 레디스에 해당 데이터 저장
        if (boardMemeBestResponseDtoList == null) {

            boardMemeBestResponseDtoList = getBestMemeResponseDtoList(categoryName);

            if (boardMemeBestResponseDtoList.size() > 0) {
                redisService.setBestMemeImgList(BEST_MEME_JJAL_KEY, boardMemeBestResponseDtoList);
                boardMemeBestResponseDtoList = redisService.getBestMemeImgList(BEST_MEME_JJAL_KEY);
            } else {
                return new ArrayList<>();
            }
        }

        // 4. 로그인한 유저라면
        // - 로그인한 유저가 명예의 밈짤 이미지들에 좋아요 눌렀는지 여부
        if(userDetails != null) {
            User user = jwtAuthenticateProcessor.getUser(userDetails);

            ObjectMapper mapper = new ObjectMapper();
            List<BoardMemeBestResponseDto> mappedList = mapper.convertValue(boardMemeBestResponseDtoList, new TypeReference<List<BoardMemeBestResponseDto>>(){});

            List<BoardMemeBestResponseDto> resultList = new ArrayList<>();
            for(BoardMemeBestResponseDto boardMemeBestResponseDto : mappedList) {
                Board board = boardRepository.findById(boardMemeBestResponseDto.getBoardId()).orElse(null);
                Long boardId = boardMemeBestResponseDto.getBoardId();
                Boolean boardLike = boardLikeRepository.existsByBoard_BoardIdAndUser(boardId, user);
                resultList.add(new BoardMemeBestResponseDto(boardMemeBestResponseDto, (long)board.getLikes().size(), boardLike));
            }

            return resultList;
        }

        return boardMemeBestResponseDtoList;
    }

    // 명예의 밈짤 데이터 DB에서 산출
    private List<BoardMemeBestResponseDto> getBestMemeResponseDtoList(String categoryName) {
        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.of(0, 0, 0)); //어제 00:00:00
        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)); //오늘 23:59:59
        NumberPath<Long> likeCnt = Expressions.numberPath(Long.class, "c");

        QBoard qBoard = QBoard.board;
        QBoardLike qBoardLike = boardLike;
        QUser qUser = QUser.user;

        List<Tuple> tupleList = queryFactory
                .select(qBoard.boardId, qBoard.thumbNail, qBoard.title, qUser.username, qBoard.thumbNail,
                        qUser.nickname, qBoard.content, qBoard.views, qBoardLike.board.count().as(likeCnt))
                .from(qBoardLike)
                .leftJoin(qBoardLike.board, qBoard)
                .leftJoin(qBoardLike.user, qUser)
                .where(qBoard.boardCategory.categoryName.eq(categoryName)
                        .and(qBoardLike.createdAt.between(startDatetime, endDatetime))
                        .and(qBoard.enabled.eq(true)))
                .groupBy(qBoardLike.board)
                .orderBy(likeCnt.desc())
                .limit(3)
                .fetch();


        List<BoardMemeBestResponseDto> boardMemeBestResponseDtoList = new ArrayList<>();

        for (Tuple tuple : tupleList) {

            boardMemeBestResponseDtoList.add(
                    BoardMemeBestResponseDto.builder()
                            .boardId(tuple.get(0, Long.class))
                            .thumbNail(tuple.get(1,String.class))
                            .title(tuple.get(2, String.class))
                            .username(tuple.get(3, String.class))
                            .profileImageUrl(tuple.get(4, String.class))
                            .writer(tuple.get(5, String.class))
                            .content(tuple.get(6, String.class))
                            .views(tuple.get(7, int.class))
                            .likeCnt(tuple.get(8, Long.class))
                            .build()
            );
        }


        return boardMemeBestResponseDtoList;
    }
    //endregion

    //region 카테고리별 게시글 총 개수
    public Long getTotalBoardCount(String categoryName) {
        BoardCategory boardCategory = getSafeBoardCategory(categoryName);

        return boardRepository.countByBoardCategoryAndEnabled(boardCategory, true);
    }
    //endregion

    //region 중복코드 정리
    private Board getSafeBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(NOT_EXIST_BOARD)
                );
    }

    private BoardCategory getSafeBoardCategory(String categoryName) {
        BoardCategory boardCategory = boardCategoryRepository.findById(categoryName.toUpperCase())
                .orElseThrow(
                        () -> new NullPointerException(NOT_EXIST_CATEGORY)
                );
        return boardCategory;
    }

    private void checkPermissionToBoard(UserDetailsImpl userDetails, Board board) {
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException(NOT_MY_BOARD);
        }
    }


    private void HashTagIsMaxFiveCheck(List<String> inputHashTagStrList) {
        if (inputHashTagStrList != null && inputHashTagStrList.size() > 5) {
            throw new IllegalArgumentException(HASHTAG_MAX_FIVE);
        }
    }
    //endregion
}