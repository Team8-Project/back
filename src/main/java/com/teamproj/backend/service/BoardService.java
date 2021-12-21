package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
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


    public BoardUploadResponseDto uploadBoard(
            UserDetailsImpl userDetails,
            BoardUploadRequestDto boardUploadRequestDto,
            String category
    ) {

        if(boardUploadRequestDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수 입력 값입니다");
        }
        if(boardUploadRequestDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 값입니다");
        }

        BoardCategory boardCategory = boardCategoryRepository.findById(category).orElseThrow(
                    () -> new NullPointerException("해당 카테고리가 없습니다.")
                );


        boardCategory = new BoardCategory(category, null);

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
                .subject(board.getBoardSubject().getSubject())
                .createdAt(board.getCreatedAt().toLocalDate())
                .build();


        return boardUploadResponseDto;
    }
}
