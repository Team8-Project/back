package com.teamproj.backend.service.board;

import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.alarm.AlarmTypeEnum;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.AlarmService;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.model.alarm.AlarmTypeEnum.SELECT_USER;

@Service
@RequiredArgsConstructor
public class BoardQuestionService {
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    private final CommentRepository commentRepository;
    private final AlarmService alarmService;

    // 댓글 채택
    public String selectAnswer(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 여부 체크,
        ValidChecker.loginCheck(userDetails);
        // 해당 댓글 조회
        Comment comment = getSafeBoardQuestionComment(commentId);
        // 댓글이 달린 게시글 정보 가져오기
        Board board = comment.getBoard();
        // 게시글 아이디 가져오기
        Long boardId = board.getBoardId();
        // 게시글 작성자
        User user = board.getUser();
        // 게시글 작성자와 로그인한 유저 같은지 체크
        // - 글 작성자만 댓글 채택 할 수 있도록
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(user.getId())) {
            throw new IllegalArgumentException(NOT_MY_BOARD);
        }
        // 댓글 채택 후 댓글 작성자에게 알림
        alarmService.sendAlarm(SELECT_USER, boardId, comment.getUser());
        // 채택 완료 메시지 Response
        return "채택 완료";
    }

    private Comment getSafeBoardQuestionComment(Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NullPointerException(NOT_EXIST_COMMENT));
    }
}
