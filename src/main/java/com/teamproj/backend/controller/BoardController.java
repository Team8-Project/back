package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.board.*;
import com.teamproj.backend.dto.board.BoardDeleteResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/api/board")
    public ResponseDto<List<BoardResponseDto>> getBoard(){
        return ResponseDto.<List<BoardResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 목록 불러오기")
                .data(boardService.getBoard("FREEBOARD"))
                .build();
    }

    @PostMapping("/api/board")
    public ResponseDto<BoardUploadResponseDto> uploadBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestBody BoardUploadRequestDto boardUploadRequestDto) {
        // To Do : 게시글 Category 추가하는 기능 만들기  => 더미 데이터만 넣은 상태
        return ResponseDto.<BoardUploadResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 작성")
                .data(boardService.uploadBoard(userDetails, boardUploadRequestDto, "FREEBOARD"))
                .build();
    }

    @GetMapping("/api/board/{postId}")
    public ResponseDto<BoardDetailResponseDto> getBoardDetail(@RequestHeader(value="Authorization", required = false) String token,
                                                                 @PathVariable Long postId) {
        if(token == null){
            token = "";
        }

        return ResponseDto.<BoardDetailResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 상세보기")
                .data(boardService.getBoardDetail(postId, token))
                .build();
    }

    @PutMapping("/api/board/{postId}")
    public ResponseDto<BoardUpdateResponseDto> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @RequestBody BoardUpdateRequestDto boardUpdateRequestDto,
                                                           @PathVariable Long postId) {
        return ResponseDto.<BoardUpdateResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 수정")
                .data(boardService.updateBoard(postId, userDetails, boardUpdateRequestDto))
                .build();
    }

    @DeleteMapping("/api/board/{postId}")
    public ResponseDto<BoardDeleteResponseDto> deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long postId) {

        return ResponseDto.<BoardDeleteResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 삭제")
                .data(boardService.deleteBoard(userDetails, postId))
                .build();
    }

    @PostMapping("/api/board/{postId}/like")
    public ResponseDto<BoardLikeResponseDto> boardLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @PathVariable Long postId) {

        return ResponseDto.<BoardLikeResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 좋아요")
                .data(boardService.boardLike(userDetails, postId))
                .build();
    }
}
