package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public List<CommentResponseDto> getCommentList(Long postId, int page, int size) {
        Optional<Board> board = boardRepository.findById(postId);
        if (!board.isPresent()) {
            throw new NullPointerException("유효하지 않은 게시글입니다.");
        }
        Page<Comment> commentPage = commentRepository.findAllByBoardOrderByCreatedAt(board.get(), PageRequest.of(page, size));
        return commentListToCommentResponseDto(commentPage.toList());
    }

    private List<CommentResponseDto> commentListToCommentResponseDto(List<Comment> commentList) {
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

    public CommentPostResponseDto postComment(UserDetailsImpl userDetails, Long postId, CommentPostRequestDto commentPostRequestDto) {
        // 로그인 여부 확인
        loginCheck(userDetails);

        Optional<Board> board = boardRepository.findById(postId);
        if (!board.isPresent()) {
            throw new NullPointerException("존재하지 않는 게시글입니다.'");
        }

        commentRepository.save(Comment.builder()
                .board(board.get())
                .content(commentPostRequestDto.getContent())
                .user(userDetails.getUser())
                .build());

        return CommentPostResponseDto.builder()
                .result("작성 성공")
                .build();
    }

    @Transactional
    public CommentPutResponseDto putComment(UserDetailsImpl userDetails, Long commentId, CommentPutRequestDto commentPutRequestDto) {
        // 로그인 여부 확인
        loginCheck(userDetails);

        Comment comment = commentIsMineCheck(userDetails, commentId);
        comment.update(commentPutRequestDto.getContent());
        commentRepository.save(comment);

        return CommentPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    public CommentDeleteResponseDto deleteComment(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 여부 확인
        loginCheck(userDetails);
        // 자신이 작성한 댓글인지 확인
        commentIsMineCheck(userDetails, commentId);

        commentRepository.deleteById(commentId);

        return CommentDeleteResponseDto.builder()
                .result("삭제 성공")
                .build();
    }

    // 보조 기능 구간
    private Comment commentIsMineCheck(UserDetailsImpl userDetails, Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (!comment.isPresent()) {
            throw new NullPointerException("존재하지 않는 댓글입니다.");
        }

        if (!userDetails.getUser().getId().equals(comment.get().getUser().getId())) {
            throw new IllegalArgumentException("자신이 작성한 댓글만 변경할 수 있습니다.");
        }

        return comment.get();
    }

    public void loginCheck(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new NullPointerException("로그인하지 않은 사용자입니다.");
        }
    }
}
