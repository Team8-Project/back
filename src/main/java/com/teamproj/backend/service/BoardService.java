package com.teamproj.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.ViewersRepository;
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
import com.teamproj.backend.dto.main.MainMemeImageResponseDto;
import com.teamproj.backend.dto.main.MainTodayBoardResponseDto;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.*;
import com.teamproj.backend.model.viewers.QViewers;
import com.teamproj.backend.model.viewers.ViewTypeEnum;
import com.teamproj.backend.model.viewers.Viewers;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.S3Uploader;
import com.teamproj.backend.util.StatisticsUtils;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
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

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.util.RedisKey.BEST_MEME_JJAL_KEY;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardImageRepository boardImageRepository;
    private final BoardTodayLikeRepository boardTodayLikeRepository;
    private final ViewersRepository viewersRepository;

    private final CommentService commentService;
    private final RedisService redisService;
    private final StatService statService;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final S3Uploader s3Uploader;

    private final JPAQueryFactory queryFactory;

    private final String S3dirName = "boardImages";

    //region 게시글 전체조회
    public List<BoardResponseDto> getBoard(String categoryName, int page, int size, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기
        User user = getSafeUserByUserDetails(userDetails);
        // 3. Request 로 넘어온 카테고리 네임 DB 에서 조회
        BoardCategory boardCategory = BoardCategory.builder().categoryName(categoryName).build();
        // 4. 카테고리와 enabled(삭제 안된) 데이터를 페이지네이션 조건에 맞게 리스트형식으로 가져오기
        List<Tuple> tupleList = getSafeBoardTupleList(user, boardCategory, page, size);

        return getBoardResponseDtoList(tupleList);
    }

    private List<Tuple> getSafeBoardTupleList(User user, BoardCategory boardCategory, int page, int size) {
        QBoard qBoard = QBoard.board;
        QBoardLike qBoardLike = QBoardLike.boardLike;
        /*
            튜플 데이터 열람
            boardId : Long, 게시글 ID
            thumbNail : String, 이미지 URL
            username : String, 사용자의 계정명
            profileImage : String, 사용자의 프로필이미지 URL
            nickname : String, 사용자의 닉네임
            createdAt : LocalDateTime, 게시글 작성 일자
            views : Integer, 조회수
            queryFactory[0] : Long, 좋아요 개수
            queryFactory[1] : Long, 좋아요 여부
             -> 좋아요 여부의 경우, 1 이상일 경우 true, 아닐 경우 false
         */
        int offset = page * size;
        return queryFactory
                .select(qBoard.boardId,
                        qBoard.thumbNail,
                        qBoard.user.username,
                        qBoard.user.profileImage,
                        qBoard.user.nickname,
                        qBoard.createdAt,
                        qBoard.views,
                        queryFactory
                                .select(qBoardLike.count())
                                .from(qBoardLike)
                                .where(qBoardLike.board.eq(qBoard)),
                        queryFactory
                                .select(qBoardLike.count())
                                .from(qBoardLike)
                                .where(qBoardLike.board.eq(qBoard),
                                        isLike(user))
                )
                .from(qBoard)
                .where(qBoard.boardCategory.eq(boardCategory),
                        qBoard.enabled.eq(true))
                .orderBy(qBoard.boardId.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    private BooleanExpression isLike(User user) {
        return user == null ?
                QBoardLike.boardLike.boardLikeId.eq(0L) :
                QBoardLike.boardLike.user.eq(user);
    }

    private User getSafeUserByUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return null;
        }
        return jwtAuthenticateProcessor.getUser(userDetails);
    }

    private List<BoardResponseDto> getBoardResponseDtoList(List<Tuple> tupleList) {
        // 5. DB 에서 받아온 게시글 List 데이터를 담을 Response Dto 생성
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        for (Tuple tuple : tupleList) {
            // Map 에 사용 될 id 키값
            Long boardId = tuple.get(0, Long.class);
            String thumbNail = tuple.get(1, String.class);
            String username = tuple.get(2, String.class);
            String profileImageUrl = tuple.get(3, String.class);
            String writer = tuple.get(4, String.class);
            LocalDateTime createdAt = tuple.get(5, LocalDateTime.class);
            Integer viewsInteger = tuple.get(6, Integer.class);
            int views = viewsInteger == null ? 0 : viewsInteger;
            Long likeCntLong = tuple.get(7, Long.class);
            int likeCnt = likeCntLong == null ? 0 : likeCntLong.intValue();
            Long isLikeLong = tuple.get(8, Long.class);
            boolean isLike = isLikeLong != null && isLikeLong > 0;

            // 7. 게시글 List 데이터를 DtoList 에 담아서 리턴
            boardResponseDtoList.add(BoardResponseDto.builder()
                    .boardId(boardId)
                    .thumbNail(thumbNail)
                    .username(username)
                    .profileImageUrl(profileImageUrl)
                    .writer(writer)
                    .createdAt(createdAt)
                    .views(views)
                    .likeCnt(likeCnt)
                    .isLike(isLike)
                    .build());
        }
        return boardResponseDtoList;
    }
    //endregion

    //region 게시글 작성
    @Transactional
    public BoardUploadResponseDto uploadBoard(UserDetailsImpl userDetails,
                                              BoardUploadRequestDto boardUploadRequestDto,
                                              String categoryName,
                                              MultipartFile multipartFile) {
        // 글 작성할려는 유저 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);
        // 1. Request 로 넘어온 데이터 유효성 검사(게시글 제목, 게시글 내용)
        String boardTitle = boardUploadRequestDto.getTitle();
        String boardContent = boardUploadRequestDto.getContent();
        if (boardTitle.isEmpty()) {
            throw new IllegalArgumentException(TITLE_IS_EMPTY);
        }
        if (boardContent.isEmpty()) {
            throw new IllegalArgumentException(CONTENT_IS_EMPTY);
        }

        // 2. Request 로 넘어온 카테고리 네임 DB 에서 조회
        BoardCategory boardCategory = getSafeBoardCategory(categoryName);
        // 3. multipartFile 로 넘어온 이미지 데이터 null 체크 => null 이 아니면 S3 버킷에 저장
        String imageUrl = "";
        if (!multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
        }

        // 4. 게시글 데이터 DB에 저장
        Board board = Board.builder()
                .title(boardTitle)                                    // 제목
                .content(boardContent)                                // 내용
                .boardCategory(boardCategory)                         // 카테고리
                .user(jwtAuthenticateProcessor.getUser(userDetails))  // 유저
                .thumbNail(imageUrl)                                  // 이미지 URL
                .enabled(true)                                        // 게시글 삭제 여부
                .build();
        boardRepository.save(board);


        // 5. 작성한 게시글에 맞는 이미지 저장
        BoardImage boardImage = BoardImage.builder()
                .board(board)
                .imageUrl(imageUrl)
                .build();
        boardImageRepository.save(boardImage);

        // 6. 저장한 데이터 기반으로 Response(게시글번호, 제목, 내용, 카테고리, 이미지 URL, 생성날짜)
        return BoardUploadResponseDto.builder()
                .boardId(board.getBoardId())                                            // 게시글아이디
                .title(board.getTitle())                                                // 제목
                .content(board.getContent())                                            // 내용
                .category(board.getBoardCategory().getCategoryName())                   // 카테고리
                .thumbNail(board.getThumbNail())                                        // 이미지 URL
                .createdAt(board.getCreatedAt() == null ? null : board.getCreatedAt())  // 게시글 생성 날짜
                .build();
    }
    //endregion

    //region 게시글 상세 조회
    public BoardDetailResponseDto getBoardDetail(Long boardId, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        User user = getSafeUserByUserDetails(userDetails);
        // 2. 게시글 조회
        Tuple boardTuple = getSafeBoardTuple(boardId, user);

        String username = boardTuple.get(1, String.class);
        String writer = boardTuple.get(2, String.class);
        String profileImageUrl = boardTuple.get(3, String.class);
        String thumbNail = boardTuple.get(4, String.class);
        LocalDateTime createdAt = boardTuple.get(5, LocalDateTime.class);
        Integer viewsInteger = boardTuple.get(6, Integer.class);
        int views = viewsInteger == null ? 0 : viewsInteger;
        Long likeCountLong = boardTuple.get(7, Long.class);
        int likeCnt = likeCountLong == null ? 0 : likeCountLong.intValue();
        Long isLikeLong = boardTuple.get(8, Long.class);
        boolean isLike = isLikeLong != null && isLikeLong > 0;
        Long viewerIpLong = boardTuple.get(9, Long.class);
        boolean isView = viewerIpLong != null && viewerIpLong > 0;

        // 4. 게시글 조회수 관련 처리 로직
        // - 조회하는 유저 IP를 통해 조회수 새로고침과 같은 중복 처리 방지
        if (!isView) {
            viewersRepository.save(Viewers.builder()
                    .viewTypeEnum(ViewTypeEnum.IMAGE_BOARD)
                    .viewerIp(StatisticsUtils.getClientIp())
                    .targetId(boardId)
                    .build());
            boardRepository.updateView(boardId);
        }

        // 6. 조회한 게시글 Response 전송
        // (게시글 아이디, 제목, 작성자 아이디, 내용, 작성자 닉네임,
        // 게시글 작성자 프로필Img, 이미지 URL, 생성날짜, 조회수, 좋아요 수, 좋아요)
        return BoardDetailResponseDto.builder()
                .boardId(boardId)                   // 게시글 아이디
                .username(username)                 // 게시글 작성유저 아이디
                .writer(writer)                     // 게시글 작성유저 닉네임
                .profileImageUrl(profileImageUrl)   // 게시글 작성유저 프로필이미지
                .thumbNail(thumbNail)               // 게시글 이미지 URL
                .createdAt(createdAt)               // 생성날짜
                .views(views)                       // 조회수
                .likeCnt(likeCnt)                   // 좋아요수
                .isLike(isLike)                     // 로그인한 유저 게시글 좋아요 여부
                .build();
    }

    private Tuple getSafeBoardTuple(Long boardId, User user) {
        QBoard qBoard = QBoard.board;
        QBoardLike qBoardLike = QBoardLike.boardLike;
        QViewers qViewers = QViewers.viewers;

        String userIp = StatisticsUtils.getClientIp();

        Tuple tuple = queryFactory
                .select(qBoard.boardId,
                        qBoard.user.username,
                        qBoard.user.nickname,
                        qBoard.user.profileImage,
                        qBoard.thumbNail,
                        qBoard.createdAt,
                        qBoard.views,
                        queryFactory
                                .select(qBoardLike.count())
                                .from(qBoardLike)
                                .where(qBoardLike.board.eq(qBoard)),
                        queryFactory
                                .select(qBoardLike.count())
                                .from(qBoardLike)
                                .where(qBoardLike.board.eq(qBoard),
                                        isLike(user)),
                        queryFactory
                                .select(qViewers.count())
                                .from(qViewers)
                                .where(qViewers.targetId.eq(boardId), qViewers.viewerIp.eq(userIp), qViewers.viewTypeEnum.eq(ViewTypeEnum.IMAGE_BOARD))
                )
                .from(qBoard)
                .where(qBoard.boardId.eq(boardId),
                        qBoard.enabled.eq(true))
                .fetchFirst();

        if (tuple == null) {
            throw new NullPointerException(NOT_EXIST_BOARD);
        }

        return tuple;
    }
    //endregion

    //region 게시글 업데이트(수정)
    @Transactional
    public BoardUpdateResponseDto updateBoard(Long boardId, UserDetailsImpl userDetails,
                                              BoardUpdateRequestDto boardUpdateRequestDto,
                                              MultipartFile multipartFile){
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 업데이트할 게시글 조회
        Board board = getSafeBoard(boardId);
        // 2. 게시글 수정 권한 체크
        checkPermissionToBoard(userDetails, board);

        // 3. multipartFile 로 넘어온 이미지 파일 저장
        // - 기존에 S3에 저장되어 있는 이미지 삭제 후
        String imageUrl = "";
        if (!multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
            deleteImg(board);
        } else {
            deleteImg(board);
        }

        // 수정
        board.update(boardUpdateRequestDto, imageUrl);
        // 수정내역 통계에 저장(수정 내용은 보관되지 않음)
        statService.statBoardModify(board);

        // 4. 게시글 저장 및 Response 전송
        boardRepository.save(board);
        return BoardUpdateResponseDto.builder()
                .result("게시글 수정 완료")
                .build();
    }

    private void deleteImg(Board board) {
        try {
            String oldImageUrl = URLDecoder.decode(
                    board.getThumbNail().replace(
                            "https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/", ""
                    ),
                    "UTF-8"
            );
            s3Uploader.deleteFromS3(oldImageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // - 결과 값 false 로 Response
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
        // 6. 결과값 true 로 Response
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
        // 1. 검색어 입력값이 empty 혹은 null 이면 Exception 발생
        if (q == null || q.isEmpty()) {
            throw new NullPointerException(SEARCH_IS_EMPTY);
        }

        // 2. 제목에 검색어가 포함되어 있는 게시글 리스트 조회
        int page = 0;
        int size = 1000;
        String category = "FREEBOARD";
        List<Board> boardList = getSaveSearchResult(q, category, page * size, size);

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
                            .build()
            );
        }

        return boardSearchResponseDtoList;
    }

    private List<Board> getSaveSearchResult(String q, String category, int page, int size) {
        if (q.length() < 2) {
            throw new IllegalArgumentException(SEARCH_MIN_SIZE_IS_TWO);
        }

        // 전문검색 쿼리 뒤의 글자도 검색 되도록.
        String newQ = q + "*";
        Optional<List<Board>> result = boardRepository.findAllByTitleAndContentByFullText(newQ, category, true, page, size);

        // 검색결과가 존재하지 않을 시 빈 리스트 return.
        return result.orElseGet(ArrayList::new);
    }
    //endregion

    // region 인기 게시글
    public List<MainTodayBoardResponseDto> getTodayBoard(int count) {
        // 1. 인기 게시글, 명예의 전당 어제 좋아요 데이터 산출 도구 데이터
        List<BoardYesterdayLikeCountRankDto> boardYesterdayLikeCountRankDtoList = getTodayBoardElement(count, "FREEBOARD");
        // 2. 인기 게시글, 명예의 전당 어제 좋아요 데이터 산출 도구 데이터 Dto 에 담아 Response
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
                .leftJoin(qBoard.user, qUser)
                .where(qBoard.boardCategory.eq(boardCategory)
                        .and(qBoardLike.createdAt.between(startDatetime, endDatetime))
                        .and(qBoard.enabled.eq(true)))
                .groupBy(qBoard.boardId)
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
        // 3. 레디스에 명예의 밈짤 데이터가 null 이면
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
        if (userDetails != null) {
            User user = jwtAuthenticateProcessor.getUser(userDetails);

            ObjectMapper mapper = new ObjectMapper();
            List<BoardMemeBestResponseDto> mappedList = mapper.convertValue(boardMemeBestResponseDtoList, new TypeReference<List<BoardMemeBestResponseDto>>() {
            });

            List<BoardMemeBestResponseDto> resultList = new ArrayList<>();
            for (BoardMemeBestResponseDto boardMemeBestResponseDto : mappedList) {
                Board board = boardRepository.findById(boardMemeBestResponseDto.getBoardId()).orElse(null);
                Long boardId = boardMemeBestResponseDto.getBoardId();
                Boolean boardLike = boardLikeRepository.existsByBoard_BoardIdAndUser(boardId, user);
                long likeCnt = board == null ? 0L : (long)board.getLikes().size();
                resultList.add(new BoardMemeBestResponseDto(boardMemeBestResponseDto, likeCnt, boardLike));
            }

            return resultList;
        }

        return boardMemeBestResponseDtoList;
    }

    // 명예의 밈짤 데이터 DB 에서 산출
    private List<BoardMemeBestResponseDto> getBestMemeResponseDtoList(String categoryName) {
        LocalDateTime startDatetime = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.of(0, 0, 0)); //어제 00:00:00
        LocalDateTime endDatetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59)); //오늘 23:59:59
        NumberPath<Long> likeCnt = Expressions.numberPath(Long.class, "c");

        QBoard qBoard = QBoard.board;
        QBoardLike qBoardLike = QBoardLike.boardLike;
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
            Integer viewsInteger = tuple.get(7, Integer.class);
            int views = viewsInteger == null ? 0 : viewsInteger;

            boardMemeBestResponseDtoList.add(
                    BoardMemeBestResponseDto.builder()
                            .boardId(tuple.get(0, Long.class))
                            .thumbNail(tuple.get(1, String.class))
                            .title(tuple.get(2, String.class))
                            .username(tuple.get(3, String.class))
                            .profileImageUrl(tuple.get(4, String.class))
                            .writer(tuple.get(5, String.class))
                            .content(tuple.get(6, String.class))
                            .views(views)
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
        return boardCategoryRepository.findById(categoryName.toUpperCase())
                .orElseThrow(
                        () -> new NullPointerException(NOT_EXIST_CATEGORY)
                );
    }

    private void checkPermissionToBoard(UserDetailsImpl userDetails, Board board) {
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException(NOT_MY_BOARD);
        }
    }
    //endregion
}