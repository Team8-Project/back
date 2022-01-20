package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.QuestionCommentLikeRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.dto.comment.CommentDeleteResponseDto;
import com.teamproj.backend.dto.comment.CommentPostRequestDto;
import com.teamproj.backend.dto.comment.CommentPostResponseDto;
import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.AlarmService;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.MemegleServiceStaticMethods;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.model.alarm.AlarmTypeEnum.RECEIVE_COMMENT;

@Service
@RequiredArgsConstructor
public class DictQuestionCommentService {
    private final DictQuestionRepository dictQuestionRepository;
    private final DictQuestionCommentRepository commentRepository;
    private final QuestionSelectRepository questionSelectRepository;
    private final QuestionCommentLikeRepository questionCommentLikeRepository;

    private final AlarmService alarmService;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    // 댓글 목록 불러오기
    public List<DictQuestionCommentResponseDto> getCommentList(Long questionId, User user, Long selectedId) {
        List<Tuple> commentTupleList = getSafeCommentTupleList(questionId, user);

        // CommentList to CommentResponseDtoList
        return commentListToCommentResponseDtoList(commentTupleList, selectedId);
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

        // 댓글 작성 시 질문 작성자에게 알림
        sendAlarmToDictQuestionWriter(user, dictQuestion);

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

    // 댓글 작성 시 질문 작성자에게 알림
    private void sendAlarmToDictQuestionWriter(User user, DictQuestion dictQuestion) {
        // - 질문 작성자가 자기 질문에 댓글 달 때는 알림 X
        if (!dictQuestion.getUser().getId().equals(user.getId())) {
            alarmService.sendAlarm(RECEIVE_COMMENT, dictQuestion.getQuestionId(), dictQuestion.getUser());
        }
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

    // 댓글 좋아요 / 좋아요 취소
    @Transactional
    public boolean likeComment(UserDetailsImpl userDetails, Long commentId) {
        // 로그인 체크
        ValidChecker.loginCheck(userDetails);
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        DictQuestionComment comment = getSafeComment(commentId);

        /*
            1. 좋아요 중일 시 : 좋아요 취소
            2. 좋아요 중이 아닐 시 : 좋아요
         */
        boolean isLike = false;
        if (questionCommentLikeRepository.existsByUserAndComment(user, comment)) {
            questionCommentLikeRepository.deleteByComment_QuestionCommentIdAndUser(commentId, user);
        } else {
            QuestionCommentLike commentLike = QuestionCommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            questionCommentLikeRepository.save(commentLike);

            isLike = true;
        }

        return isLike;
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

    // 채택된 댓글인지 체크하는 기능
    private void checkSelected(DictQuestionComment comment) {
        if (questionSelectRepository.existsByQuestionComment(comment)) {
            throw new IllegalArgumentException(ALREADY_SELECT);
        }
    }

    // Get SafeEntity
    // Comment
    private DictQuestionComment getSafeComment(Long commentId) {
        Optional<DictQuestionComment> comment = commentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NullPointerException(NOT_EXIST_COMMENT));
    }

    // CommentList
    private List<Tuple> getSafeCommentTupleList(Long questionId, User user) {
        QDictQuestionComment qComment = QDictQuestionComment.dictQuestionComment;
        QQuestionCommentLike qQuestionCommentLike = QQuestionCommentLike.questionCommentLike;

        return queryFactory
                .select(qComment.questionCommentId,
                        qComment.user.profileImage,
                        qComment.user.username,
                        qComment.user.nickname,
                        qComment.content,
                        qComment.createdAt,
                        qComment.questionCommentLike.size(),
                        queryFactory
                                .select(qQuestionCommentLike.count())
                                .from(qQuestionCommentLike)
                                .where(qQuestionCommentLike.comment.eq(qComment),
                                        isCommentLike(user)))
                .from(qComment)
                .where(qComment.dictQuestion.questionId.eq(questionId)
                        .and(qComment.enabled.eq(true)))
                .orderBy(qComment.questionCommentId.asc())
                .fetch();
    }

    private BooleanExpression isCommentLike(User user) {
        return user == null ?
                QQuestionCommentLike.questionCommentLike.dictLikeId.eq(0L) :
                QQuestionCommentLike.questionCommentLike.user.eq(user);
    }

    // Entity to Dto
    // CommentList to CommentResponseDtoList
    private List<DictQuestionCommentResponseDto> commentListToCommentResponseDtoList(List<Tuple> tupleList,
                                                                                     Long selectedId) {
        List<DictQuestionCommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Tuple tuple : tupleList) {
            Long commentId = tuple.get(0, Long.class);
            String profileImage = tuple.get(1, String.class);
            String writerId = tuple.get(2, String.class);
            String writer = tuple.get(3, String.class);
            String content = tuple.get(4, String.class);
            LocalDateTime createdAt = tuple.get(5, LocalDateTime.class);
            Integer likeCountInteger = tuple.get(6, Integer.class);
            int likeCount = likeCountInteger == null ? 0 : likeCountInteger;
            Long isLikeLong = tuple.get(7, Long.class);
            Boolean isLike = isLikeLong != null && isLikeLong > 0;

            commentResponseDtoList.add(DictQuestionCommentResponseDto.builder()
                    .commentId(commentId)
                    .commentWriterId(writerId)
                    .commentWriter(writer)
                    .profileImageUrl(profileImage)
                    .commentContent(content)
                    .createdAt(createdAt)
                    .isLike(isLike)
                    .likeCount(likeCount)
                    .isSelected(commentId.equals(selectedId))
                    .build());
        }

        return commentResponseDtoList;
    }

    private HashMap<String, Boolean> getLikeMap(List<Long> commentIdList, User user) {
        QQuestionCommentLike qQuestionCommentLike = QQuestionCommentLike.questionCommentLike;
        List<Tuple> likeTuple = queryFactory
                .select(qQuestionCommentLike.comment.questionCommentId, qQuestionCommentLike.user.id)
                .from(qQuestionCommentLike)
                .where(eqUser(user),
                        qQuestionCommentLike.comment.questionCommentId.in(commentIdList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(likeTuple);
    }

    // qQuestionCommentLike 와 user 를 비교하기 위한 BooleanExpression.
    private BooleanExpression eqUser(User user) {
        QQuestionCommentLike qQuestionCommentLike = QQuestionCommentLike.questionCommentLike;
        if (user == null) {
            return null;
        }
        return qQuestionCommentLike.user.eq(user);
    }
    // endregion
}