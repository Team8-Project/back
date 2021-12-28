package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.*;
import com.teamproj.backend.dto.board.*;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.board.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardImageRepository boardImageRepository;
    private final BoardHashTagRepository boardHashTagRepository;

    private final CommentService commentService;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final S3Uploader s3Uploader;

    private final String imageDirName = "boardImages";


    //region 게시글 전체조회
    public List<BoardResponseDto> getBoard(String categoryName) {
        Optional<BoardCategory> boardCategory = boardCategoryRepository.findById(categoryName.toUpperCase());
        if (!boardCategory.isPresent()) {
            throw new NullPointerException(ExceptionMessages.NOT_EXIST_CATEGORY);
        }

        Optional<List<Board>> boardList = boardRepository.findAllByBoardCategoryAndEnabled(boardCategory.get(), true);
        return boardList.map(this::boardListToBoardResponseDtoList).orElseGet(ArrayList::new);
    }

    private List<BoardResponseDto> boardListToBoardResponseDtoList(List<Board> boardList) {
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        for (Board board : boardList) {
            boardResponseDtoList.add(BoardResponseDto.builder()
                    .boardId(board.getBoardId())
                    .thumbNail(board.getThumbNail())
                    .title(board.getTitle())
                    .username(board.getUser().getUsername())
                    .writer(board.getUser().getNickname())
                    .content(board.getContent())
                    .createdAt(board.getCreatedAt().toLocalDate())
                    .views(board.getViews())
                    .likeCnt(board.getLikes().size())
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

        if (boardUploadRequestDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException(ExceptionMessages.TITLE_IS_EMPTY);
        }
        if (boardUploadRequestDto.getContent().isEmpty()) {
            throw new IllegalArgumentException(ExceptionMessages.CONTENT_IS_EMPTY);
        }

        if (boardUploadRequestDto.getHashTags() != null && boardUploadRequestDto.getHashTags().size() > 5) {
            throw new IllegalArgumentException(ExceptionMessages.HASHTAG_MAX_FIVE);
        }

        BoardCategory boardCategory = boardCategoryRepository.findById(categoryName.toUpperCase())
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_CATEGORY)
                );

        String imageUrl = "";
        if (multipartFile != null) {
            imageUrl = s3Uploader.upload(multipartFile, imageDirName);
        }


        Board board = Board.builder()
                .title(boardUploadRequestDto.getTitle())
                .content(boardUploadRequestDto.getContent())
                .boardCategory(boardCategory)
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .thumbNail("https://img.insight.co.kr/static/2021/12/04/700/img_20211204160105_7381lxd4.webp")
                .enabled(true)
                .build();
        boardRepository.save(board);



        List<BoardHashTag> boardHashTagList = new ArrayList<>();
        if(boardUploadRequestDto.getHashTags() != null) {
            for(String hashTag : boardUploadRequestDto.getHashTags()) {
                BoardHashTag boardHashTag = BoardHashTag.builder()
                        .hashTagName(hashTag)
                        .board(board)
                        .build();

                boardHashTagList.add(boardHashTag);
                boardHashTagRepository.save(boardHashTag);
            }

            board.setHashTagList(boardHashTagList);
            boardRepository.save(board);
        }


        BoardImage boardImage = BoardImage.builder()
                .board(board)
                .imageUrl("https://img.insight.co.kr/static/2021/12/04/700/img_20211204160105_7381lxd4.webp")
                .build();

        boardImageRepository.save(boardImage);


        return BoardUploadResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getBoardCategory().getCategoryName())
                .thumbNail(board.getThumbNail())
                .createdAt(board.getCreatedAt() == null ? null :  board.getCreatedAt().toLocalDate())
                .hashTags(boardHashTagList == null ? null : boardHashTagList.stream().map(
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
        if(userDetails != null) {
            Optional<BoardLike> boardLike = boardLikeRepository.findByBoardAndUser(
                    board, jwtAuthenticateProcessor.getUser(userDetails)
            );

            if(boardLike.isPresent()) {
                isLike = true;
            }
        }

        boardRepository.updateView(boardId);

        List<BoardLike> boardLikeList = boardLikeRepository.findAllByBoard(board);

        return BoardDetailResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getNickname())
                .createdAt(board.getCreatedAt().toLocalDate())
                .views(board.getViews())
                .likeCnt(boardLikeList.size())
                .isLike(isLike)
                .commentList(commentService.getCommentList(board.getBoardId(), 0, 10))
                .build();
    }
    //endregion

    //region 게시글 업데이트(수정)
    public BoardUpdateResponseDto updateBoard(Long boardId, UserDetailsImpl userDetails,
                                              BoardUpdateRequestDto boardUpdateRequestDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(
                        () -> new NullPointerException(ExceptionMessages.NOT_EXIST_BOARD)
                );
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(board.getUser().getId())) {
            throw new IllegalArgumentException(ExceptionMessages.NOT_MY_BOARD);
        }

        board.update(boardUpdateRequestDto);

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

            return BoardLikeResponseDto.builder()
                    .result(false)
                    .build();
        }

        BoardLike boardLike = BoardLike.builder()
                .board(board)
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .build();

        boardLikeRepository.save(boardLike);

        return BoardLikeResponseDto.builder()
                .result(true)
                .build();
    }
    //endregion

    //region 게시글 검색
    public List<BoardSearchResponseDto> boardSearch(String q) {
        if(q == null || q.isEmpty()) {
            throw new NullPointerException(ExceptionMessages.SEARCH_IS_EMPTY);
        }

        Optional<List<Board>> boardList = boardRepository.findByTitleContaining(q);


        if(boardList.get().size() == 0) {
            throw new NullPointerException(ExceptionMessages.SEARCH_BOARD_IS_EMPTY);
        }


        List<BoardSearchResponseDto> boardSearchResponseDtoList = new ArrayList<>();
        for(Board board : boardList.get()) {
            boardSearchResponseDtoList.add(
                    BoardSearchResponseDto.builder()
                            .boardId(board.getBoardId())
                            .thumbNail(board.getThumbNail())
                            .title(board.getTitle())
                            .username(board.getUser().getUsername())
                            .writer(board.getUser().getNickname())
                            .content(board.getContent())
                            .createdAt(board.getCreatedAt().toLocalDate())
                            .views(board.getViews())
                            .likeCnt(board.getLikes().size())
                            .hashTags(board.getBoardHashTagList() == null ? null : board.getBoardHashTagList().stream().map(
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
        List<BoardHashTag> boardHashTagList = boardHashTagRepository.boardHashTagList();

        if(boardHashTagList.size() == 0) {
            throw new IllegalArgumentException(ExceptionMessages.HASHTAG_IS_EMPTY);
        }

        return BoardHashTagResponseDto.builder().hashTags(boardHashTagList.stream().map(
                h -> h.getHashTagName()).collect(Collectors.toCollection(ArrayList::new))
        ).build();
    }
    //endregion
}
