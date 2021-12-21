package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.model.board.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public List<BoardResponseDto> getBoard() {
        List<Board> boardList = boardRepository.findAll();
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
}
