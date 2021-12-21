package com.teamproj.backend.controller;

import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

//    @GetMapping("/api/boards/subject")
//    public List<BoardSubjectResponseDto> getBoardSubject(String categoryName){
//
//    }

    @GetMapping("/api/boards/")
    public List<BoardResponseDto> getBoard(){
        return boardService.getBoard("FREEBOARD");
    }
}
