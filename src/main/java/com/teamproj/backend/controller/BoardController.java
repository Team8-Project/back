package com.teamproj.backend.controller;

import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.board.*;
import com.teamproj.backend.dto.board.BoardDelete.BoardDeleteResponseDto;
import com.teamproj.backend.dto.board.BoardDetail.BoardDetailResponseDto;
import com.teamproj.backend.dto.board.BoardLike.BoardLikeResponseDto;
import com.teamproj.backend.dto.board.BoardMemeBest.BoardMemeBestResponseDto;
import com.teamproj.backend.dto.board.BoardSearch.BoardSearchResponseDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateResponseDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUpload.BoardUploadResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/api/board/list/{categoryName}")
    public ResponseDto<List<BoardResponseDto>> getBoard(@PathVariable String categoryName,
                                                        @RequestParam("page") int page,
                                                        @RequestParam("size") int size,
                                                        @RequestHeader(value="Authorization", required = false) String token ){
        return ResponseDto.<List<BoardResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 목록 불러오기")
                .data(boardService.getBoard(categoryName, page, size, token))
                .build();
    }

    @PostMapping("/api/board/{categoryName}")
    public ResponseDto<BoardUploadResponseDto> uploadBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable String categoryName,
                                                           @RequestPart BoardUploadRequestDto boardUploadRequestDto,
                                                           @RequestPart(value = "thumbNail", required = false) MultipartFile multipartFile
                                                            ) throws IOException {
        return ResponseDto.<BoardUploadResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 작성")
                .data(boardService.uploadBoard(userDetails, boardUploadRequestDto, categoryName, multipartFile))
                .build();
    }

    @GetMapping("/api/board/{boardId}")
    public ResponseDto<BoardDetailResponseDto> getBoardDetail(@RequestHeader(value="Authorization", required = false) String token,
                                                              @PathVariable Long boardId) {
        return ResponseDto.<BoardDetailResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 상세보기")
                .data(boardService.getBoardDetail(boardId, token))
                .build();
    }

    @PutMapping("/api/board/{boardId}")
    public ResponseDto<BoardUpdateResponseDto> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long boardId,
                                                           @RequestPart BoardUpdateRequestDto boardUpdateRequestDto,
                                                           @RequestPart(value = "thumbNail", required = false) MultipartFile multipartFile
    ) throws IOException {
        return ResponseDto.<BoardUpdateResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 수정")
                .data(boardService.updateBoard(boardId, userDetails, boardUpdateRequestDto, multipartFile))
                .build();
    }

    @DeleteMapping("/api/board/{boardId}")
    public ResponseDto<BoardDeleteResponseDto> deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @PathVariable Long boardId) {

        return ResponseDto.<BoardDeleteResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 삭제")
                .data(boardService.deleteBoard(userDetails, boardId))
                .build();
    }

    @GetMapping("/api/board/{boardId}/like")
    public ResponseDto<BoardLikeResponseDto> boardLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable Long boardId) {
        return ResponseDto.<BoardLikeResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 좋아요")
                .data(boardService.boardLike(userDetails, boardId))
                .build();
    }

    @GetMapping("/api/board/search")
    public ResponseDto<List<BoardSearchResponseDto>> boardSearch(@RequestParam String q) {

        return ResponseDto.<List<BoardSearchResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 검색 요청")
                .data(boardService.boardSearch(q))
                .build();
    }

    @GetMapping("/api/board/{categoryName}/best")
    public ResponseDto<List<BoardMemeBestResponseDto>> getBestMeme(@RequestHeader(value="Authorization", required = false) String token,
                                                                   @PathVariable String categoryName) {
        return ResponseDto.<List<BoardMemeBestResponseDto>>builder()
                .status(HttpStatus.OK.toString())
                .message("명예의 밈짤 받기 요청")
                .data(boardService.getBestMemeImg(categoryName, token))
                .build();
    }

    @GetMapping("/api/board/count/{categoryName}")
    public ResponseDto<Long> getTotalBoardCount(@PathVariable String categoryName) {
        return ResponseDto.<Long>builder()
                .status(HttpStatus.OK.toString())
                .message("게시글 총 개수 출력 요청")
                .data(boardService.getTotalBoardCount(categoryName))
                .build();
    }
}
