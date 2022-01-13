package com.teamproj.backend.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.dto.comment.*;
import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.QComment;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.model.dict.question.QDictQuestionComment;
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
import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_BOARD;

@Service
@RequiredArgsConstructor
public class DictQuestionCommentService {
    private final DictQuestionRepository dictQuestionRepository;
    private final DictQuestionCommentRepository commentRepository;
    private final QuestionSelectRepository questionSelectRepository;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    // 댓글 목록 불러오기
    public List<DictQuestionCommentResponseDto> getCommentList(DictQuestion dictQuestion) {
        List<DictQuestionComment> commentList = getSafeCommentList(dictQuestion);

        // CommentList to CommentResponseDtoList
        return commentListToCommentResponseDtoList(commentList);
    }

    // 댓글 작성
    public CommentPostResponseDto postComment(UserDetailsImpl userDetails, Long questionId, CommentPostRequestDto commentPostRequestDto) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        User user = jwtAuthenticateProcessor.getUser(userDetails);
        DictQuestion dictQuestion = getSafeQuestion(questionId);
        DictQuestionComment comment = commentRepository.save(DictQuestionComment.builder()
                .dictQuestion(dictQuestion)
                .content(commentPostRequestDto.getContent())
                .user(user)
                .enabled(true)
                .build());

        // Comment to CommentPostResponseDto
        return CommentPostResponseDto.builder()
                .commentId(comment.getQuestionCommentId())
                .profileImageUrl(user.getProfileImage())
                .commentWriterId(user.getUsername())
                .commentWriter(user.getNickname())
                .commentContent(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private DictQuestion getSafeQuestion(Long questionId) {
        Optional<DictQuestion> question = dictQuestionRepository.findById(questionId);
        return question.orElseThrow(() -> new NullPointerException(NOT_EXIST_QUESTION));
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponseDto deleteComment(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);

        // 자신이 작성한 댓글인지 확인
        DictQuestionComment comment = commentIsMineCheck(userDetails, commentId);

        checkSelected(comment);
        // enabled 를 false 로 하여 삭제 처리. 이후 쿼리에서 조회되지 않음!
        comment.setEnabled(false);

        return CommentDeleteResponseDto.builder()
                .result("삭제 성공")
                .build();
    }

    private void checkSelected(DictQuestionComment comment) {
        if (questionSelectRepository.existsByQuestionComment(comment)) {
            throw new IllegalArgumentException(ALREADY_SELECT);
        }
    }


    // region 보조 기능
    // Utils
    // 자신의 댓글인지 체크하는 기능
    private DictQuestionComment commentIsMineCheck(UserDetailsImpl userDetails, Long commentId) {
        DictQuestionComment comment = getSafeComment(commentId);
        if (!jwtAuthenticateProcessor.getUser(userDetails).getId().equals(comment.getUser().getId())) {
            throw new IllegalArgumentException(NOT_MY_COMMENT);
        }

        // 내 댓글이 맞으면 그 댓글의 정보를 반환해 줌.
        return comment;
    }

    // Get SafeEntity
    // Comment
    private DictQuestionComment getSafeComment(Long commentId) {
        Optional<DictQuestionComment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NullPointerException(NOT_EXIST_COMMENT));
    }

    // CommentList
    private List<DictQuestionComment> getSafeCommentList(DictQuestion dictQuestion) {
        if(dictQuestion == null){
            throw new NullPointerException(NOT_EXIST_BOARD);
        }

        QDictQuestionComment qComment = QDictQuestionComment.dictQuestionComment;
        QUser qUser = QUser.user;

        return queryFactory.selectFrom(qComment).distinct()
                .leftJoin(qComment.user, qUser)
                .fetchJoin()
                .where(qComment.dictQuestion.eq(dictQuestion)
                        .and(qComment.enabled.eq(true)))
                .orderBy(qComment.createdAt.asc())
                .fetch();
    }

    // Entity to Dto
    // CommentList to CommentResponseDtoList
    private List<DictQuestionCommentResponseDto> commentListToCommentResponseDtoList(List<DictQuestionComment> commentList) {
        List<DictQuestionCommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (DictQuestionComment comment : commentList) {
            User user = comment.getUser();
            commentResponseDtoList.add(DictQuestionCommentResponseDto.builder()
                    .commentId(comment.getQuestionCommentId())
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
