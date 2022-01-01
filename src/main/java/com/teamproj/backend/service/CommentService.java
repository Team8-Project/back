package com.teamproj.backend.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.QComment;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.QBoard;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
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
    private final JPAQueryFactory queryFactory;

    // 댓글 목록 불러오기
    public List<CommentResponseDto> getCommentList(Board board) {
        List<Comment> commentList = getSafeCommentList(board);

        // CommentList to CommentResponseDtoList
        return commentListToCommentResponseDtoList(commentList);
    }



    // 댓글 작성
    public CommentPostResponseDto postComment(UserDetailsImpl userDetails, Long boardId, CommentPostRequestDto commentPostRequestDto) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Board board = getSafeBoard(boardId);
        Comment comment = commentRepository.save(Comment.builder()
                .board(board)
                .content(commentPostRequestDto.getContent())
                .user(user)
                .enabled(true)
                .build());

        // Comment to CommentPostResponseDto
        return CommentPostResponseDto.builder()
                .commentId(comment.getCommentId())
                .profileImageUrl("")
                .commentWriterId(user.getUsername())
                .commentWriter(user.getNickname())
                .commentContent(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // 댓글 수정
    @Transactional
    public CommentPutResponseDto putComment(UserDetailsImpl userDetails, Long commentId, CommentPutRequestDto commentPutRequestDto) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        // 수정하려는 댓글이 나의 댓글인지 확인
        Comment comment = commentIsMineCheck(userDetails, commentId);
        // 수정 후 적용
        comment.update(commentPutRequestDto.getContent());

        return CommentPutResponseDto.builder()
                .result("수정 성공")
                .build();
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponseDto deleteComment(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        // 자신이 작성한 댓글인지 확인
        Comment comment = commentIsMineCheck(userDetails, commentId);
        // enabled 를 false 로 하여 삭제 처리. 이후 쿼리에서 조회되지 않음!
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

        // 내 댓글이 맞으면 그 댓글의 정보를 반환해 줌.
        return comment;
    }

    // Get SafeEntity
    // Board
    private Board getSafeBoard(Long boardId) {
        Optional<Board> board = boardRepository.findById(boardId);
        return board.orElseThrow(() -> new NullPointerException(NOT_EXIST_BOARD));
    }

    // Comment
    private Comment getSafeComment(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NullPointerException(NOT_EXIST_COMMENT));
    }

    // CommentList
    private List<Comment> getSafeCommentList(Board board) {
        if(board == null){
            throw new NullPointerException(NOT_EXIST_BOARD);
        }

        QComment qComment = QComment.comment;
        QBoard qBoard = QBoard.board;
        QUser qUser = QUser.user;

        return queryFactory.selectFrom(qComment)
                .leftJoin(qComment.board, qBoard)
                .fetchJoin()
                .leftJoin(qComment.user, qUser)
                .fetchJoin()
                .where(qComment.board.eq(board)
                        .and(qComment.enabled.eq(true)))
                .orderBy(qComment.createdAt.asc())
                .fetch();
    }

    // Entity to Dto
    // CommentList to CommentResponseDtoList
    private List<CommentResponseDto> commentListToCommentResponseDtoList(List<Comment> commentList) {
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Comment comment : commentList) {
            User user = comment.getUser();
            commentResponseDtoList.add(CommentResponseDto.builder()
                    .commentId(comment.getCommentId())
                    .commentWriterId(user.getUsername())
                    .commentWriter(user.getNickname())
                    .commentContent(comment.getContent())
                    .profileImageUrl(user.getProfileImage())
                    .createdAt(comment.getCreatedAt())
                    .build());
        }

        return commentResponseDtoList;
    }
    // endregion
}
