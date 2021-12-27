package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    public List<CommentResponseDto> getCommentList(Long postId, int page, int size) {
        Board board = getSafeBoard(postId);
        Page<Comment> commentPage = commentRepository.findAllByBoardAndEnabledOrderByCreatedAt(board, true, PageRequest.of(page, size));

        return commentListToCommentResponseDtoList(commentPage.toList());
    }

    public CommentPostResponseDto postComment(UserDetailsImpl userDetails, Long postId, CommentPostRequestDto commentPostRequestDto) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        Board board = getSafeBoard(postId);
        Comment comment = commentRepository.save(Comment.builder()
                .board(board)
                .content(commentPostRequestDto.getContent())
                .user(jwtAuthenticateProcessor.getUser(userDetails))
                .enabled(true)
                .build());

        return CommentPostResponseDto.builder()
                .commentId(comment.getCommentId())
                .profileImageUrl("")
                .commentWriterId(comment.getUser().getUsername())
                .commentWriter(comment.getUser().getNickname())
                .commentContent(comment.getContent())
                .createdAt(comment.getCreatedAt().toLocalDate())
                .build();
    }

    @Transactional
    public CommentPutResponseDto putComment(UserDetailsImpl userDetails, Long commentId, CommentPutRequestDto commentPutRequestDto) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        Comment comment = commentIsMineCheck(userDetails, commentId);
        comment.update(commentPutRequestDto.getContent());
        commentRepository.save(comment);

        return CommentPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    @Transactional
    public CommentDeleteResponseDto deleteComment(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);
        // 자신이 작성한 댓글인지 확인
        commentIsMineCheck(userDetails, commentId);

        // enabled 를 false 로 하여 삭제처리. 이후 쿼리에서 조회되지 않음!
        Comment comment = getSafeComment(commentId);
        comment.setEnabled(false);

        return CommentDeleteResponseDto.builder()
                .result("삭제 성공")
                .build();
    }


    // region 보조 기능
    // Utils
    // 자신의 댓글인지 체크하는 기능
    private Comment commentIsMineCheck(UserDetailsImpl userDetails, Long commentId) {
        Comment comment = getSafeComment(commentId);
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(comment.getUser().getId())) {
            throw new IllegalArgumentException(NOT_MY_COMMENT);
        }

        return comment;
    }

    // Get SafeEntity
    // Board
    private Board getSafeBoard(Long postId) {
        Optional<Board> board = boardRepository.findById(postId);
        if (!board.isPresent()) {
            throw new NullPointerException(NOT_EXIST_BOARD);
        }
        return board.get();
    }

    // Comment
    private Comment getSafeComment(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (!comment.isPresent()) {
            throw new NullPointerException(NOT_EXIST_COMMENT);
        }

        return comment.get();
    }

    // Entity to Dto
    // CommentList to CommentResponseDtoList
    private List<CommentResponseDto> commentListToCommentResponseDtoList(List<Comment> commentList) {
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Comment comment : commentList) {
            commentResponseDtoList.add(CommentResponseDto.builder()
                    .commentId(comment.getCommentId())
                    .commentWriterId(comment.getUser().getUsername())
                    .commentWriter(comment.getUser().getNickname())
                    .commentContent(comment.getContent())
                    .profileImageUrl("")
                    .createdAt(comment.getCreatedAt().toLocalDate())
                    .build());
        }

        return commentResponseDtoList;
    }
    // endregion
}
