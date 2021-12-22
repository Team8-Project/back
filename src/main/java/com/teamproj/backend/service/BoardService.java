package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.board.BoardDetailResponseDto;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUploadResponseDto;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    public List<BoardResponseDto> getBoard(String category) {
        Optional<BoardCategory> boardCategory = boardCategoryRepository.findById(category.toUpperCase());
        if (!boardCategory.isPresent()) {
            throw new NullPointerException("유효한 카테고리가 아닙니다.");
        }

        Optional<List<Board>> boardList = boardRepository.findAllByBoardCategory(boardCategory.get());
        if (!boardList.isPresent()) {
            return new ArrayList<>();
        }

        return boardListToBoardResponseDtoList(boardList.get());
    }

    private List<BoardResponseDto> boardListToBoardResponseDtoList(List<Board> boardList) {
        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();

        for (Board board : boardList) {
            boardResponseDtoList.add(BoardResponseDto.builder()
                    .postId(board.getPostId())
                    .nickname(board.getUser().getNickname())
                    .subject(board.getBoardSubject().getSubject())
                    .content(board.getContent())
                    .createdAt(board.getCreatedAt().toLocalDate())
                    .build());
        }

        return boardResponseDtoList;
    }


    public BoardUploadResponseDto uploadBoard(UserDetailsImpl userDetails, BoardUploadRequestDto boardUploadRequestDto,
                                              String category) {

        if(boardUploadRequestDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 값입니다");
        }
        if(boardUploadRequestDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 값입니다");
        }

        // To Do: 카테고리 추가되면 조회 후 해당 카테고리 Response
//        BoardCategory boardCategory = boardCategoryRepository.findById(category).orElseThrow(
//                    () -> new NullPointerException("해당 카테고리가 없습니다.")
//                );


        BoardCategory boardCategory = new BoardCategory(category, null);

        // 테스트용 BoardCategory 저장 => 나중에 삭제 필요
        boardCategoryRepository.save(boardCategory);

        Board board = Board.builder()
                        .title(boardUploadRequestDto.getTitle())
                        .content(boardUploadRequestDto.getContent())
                        .boardCategory(boardCategory)
                        .user(userDetails.getUser())
                        .build();
        boardRepository.save(board);

        BoardUploadResponseDto boardUploadResponseDto = BoardUploadResponseDto.builder()
                .boardId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
//                .subject(board.getBoardSubject().getSubject())
                .createdAt(board.getCreatedAt().toLocalDate())
                .build();


        return boardUploadResponseDto;
    }

    public BoardDetailResponseDto getBoardDetail(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(
                        () -> new NullPointerException("해당 게시글이 없습니다.")
                );

        System.out.println(board.getTitle());
        boardRepository.updateView(postId);

        return BoardDetailResponseDto.builder()
                .boardId(board.getPostId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getUser().getNickname())
                .createdAt(board.getCreatedAt().toLocalDate())
                .subject(board.getBoardSubject().getSubject())
                .views(board.getViews())
//                .likeCnt(board.)
//                .commentList()
                .build();
    }

    public String updateBoard(Long postId, UserDetailsImpl userDetails, BoardUploadRequestDto boardUploadRequestDto) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(
                        () -> new NullPointerException("해당 게시글이 없습니다.")
                );
        if(userDetails.getUser().getId() != board.getUser().getId()) {
            throw new IllegalArgumentException("게시글을 작성한 유저만 수정이 가능합니다.");
        }

        // To Do: 글머리도 수정 가능할 시 추가 작성 필요
        board.update(boardUploadRequestDto);

        boardRepository.save(board);
        return "게시글 수정 완료";
    }

    public String deleteBoard(UserDetailsImpl userDetails, Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(
                        () -> new NullPointerException("해당 게시글이 없습니다.")
                );
        if(userDetails.getUser().getId() != board.getUser().getId()) {
            throw new IllegalArgumentException("게시글을 작성한 유저만 삭제가 가능합니다.");
        }

        boardRepository.delete(board);
        return "게시글 삭제 완료";
    }
}
