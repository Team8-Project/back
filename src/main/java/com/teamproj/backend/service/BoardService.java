package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    public List<BoardResponseDto> getBoard(String category) {
        Optional<BoardCategory> boardCategory = boardCategoryRepository.findById(category.toUpperCase());
        if(!boardCategory.isPresent()){
            throw new NullPointerException("유효한 카테고리가 아닙니다.");
        }

        Optional<List<Board>> boardList = boardRepository.findAllByBoardCategory(boardCategory.get());
        if(!boardList.isPresent()){
            return new ArrayList<>();
        }

        List<BoardResponseDto> boardResponseDtoList = new ArrayList<>();
        for (Board board : boardList.get()) {
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
}
