package com.teamproj.backend.controller;

import com.teamproj.backend.dto.board.*;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/api/board/subject")
    public ResponseEntity<List<BoardSubjectResponseDto>> getBoardSubject(String categoryName){
        return ResponseEntity.ok()
                .body(boardService.getBoardSubject("FREEBOARD"));
    }

    @GetMapping("/api/board")
    public List<BoardResponseDto> getBoard(){
        return boardService.getBoard("FREEBOARD");
    }

    @PostMapping("/api/board")
    public ResponseEntity<BoardUploadResponseDto> uploadBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestBody BoardUploadRequestDto boardUploadRequestDto) {
        // To Do : 게시글 subject, Category 추가하는 기능 만들기  => 더미 데이터만 넣은 상태
        return ResponseEntity.ok()
                .body(boardService.uploadBoard(userDetails, boardUploadRequestDto, "FREEBOARD"));
    }

    @GetMapping("/api/board/{postId}")
    public ResponseEntity<BoardDetailResponseDto> getBoardDetail(@RequestHeader(value="Authorization", required = false) String token,
                                                                 @PathVariable Long postId) {
        if(token == null){
            token = "";
        }
        System.out.println("token" + token);
        return ResponseEntity.ok()
                .body(boardService.getBoardDetail(postId, token));
    }

    @PutMapping("/api/board/{postId}")
    public ResponseEntity<String> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody BoardUploadRequestDto boardUploadRequestDto,
                                              @PathVariable Long postId) {
        return ResponseEntity.ok()
                .body(boardService.updateBoard(postId, userDetails, boardUploadRequestDto));
    }

    @DeleteMapping("/api/board/{postId}")
    public ResponseEntity<String> deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable Long postId) {

        System.out.println(postId);
        return ResponseEntity.ok()
                .body(boardService.deleteBoard(userDetails, postId));
    }
}
