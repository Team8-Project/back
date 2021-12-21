package com.teamproj.backend.controller;

import com.teamproj.backend.dto.board.BoardResponseDto;
import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUploadResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/api/board")
    public ResponseEntity<BoardUploadResponseDto> uploadBoard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            BoardUploadRequestDto boardUploadRequestDto
    ) {
        // To Do : 게시글 subject, Category 추가하는 기능 만들기  => 더미 데이터만 넣은 상태
        return ResponseEntity.ok()
                .body(boardService.uploadBoard(userDetails, boardUploadRequestDto, "FREEBOARD"));
    }
}
