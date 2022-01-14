package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.QuestionCommentLikeRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.dto.comment.CommentDeleteResponseDto;
import com.teamproj.backend.dto.comment.CommentPostRequestDto;
import com.teamproj.backend.dto.comment.CommentPostResponseDto;
import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import com.teamproj.backend.model.QUser;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.*;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import com.teamproj.backend.util.MemegleServiceStaticMethods;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictQuestionCommentService {
    private final DictQuestionRepository dictQuestionRepository;
    private final DictQuestionCommentRepository commentRepository;
    private final QuestionSelectRepository questionSelectRepository;
    private final QuestionCommentLikeRepository questionCommentLikeRepository;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final JPAQueryFactory queryFactory;

    // 댓글 목록 불러오기
    public List<DictQuestionCommentResponseDto> getCommentList(DictQuestion dictQuestion, User user) {
        List<DictQuestionComment> commentList = getSafeCommentList(dictQuestion);

        // CommentList to CommentResponseDtoList
        return commentListToCommentResponseDtoList(commentList, user);
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
    private List<DictQuestionComment> getSafeCommentList(DictQuestion dictQuestion) {
        if (dictQuestion == null) {
            throw new NullPointerException(NOT_EXIST_BOARD);
        }

        QDictQuestionComment qComment = QDictQuestionComment.dictQuestionComment;
        QUser qUser = QUser.user;

        return queryFactory.selectFrom(qComment).distinct()
                .leftJoin(qComment.user, qUser)
                .fetchJoin()
                .where(qComment.dictQuestion.eq(dictQuestion)
                        .and(qComment.enabled.eq(true)))
                .orderBy(qComment.questionCommentLike.size().desc(), qComment.createdAt.asc())
                .fetch();
    }

    // Entity to Dto
    // CommentList to CommentResponseDtoList
    private List<DictQuestionCommentResponseDto> commentListToCommentResponseDtoList(List<DictQuestionComment> commentList,
                                                                                     User user) {
        List<DictQuestionCommentResponseDto> commentResponseDtoList = new ArrayList<>();

        // 작성자 맵
        // id:username : username / id:nickname : nickname / id:profileImage : profileImage
        HashMap<String, String> writerMap = getUserInfoMap(commentList);
        // 좋아요 목록 맵
        // commentId:userId : 값이 존재할 시 좋아요, 아니면 아님
        HashMap<String, Boolean> likeMap = getLikeMap(commentList);
        // 좋아요 개수 맵
        HashMap<Long, Long> likeCountMap = getLikeCountMap(commentList);
        // 채택 여부
        Long selectedComment = getSelectedComment(commentList);

        for (DictQuestionComment comment : commentList) {
            Long commentId = comment.getQuestionCommentId();

            // likeMap 에 값이 있음 = true, 없음 = false
            boolean isLike = false;
            if (user != null) {
                isLike = likeMap.get(commentId + ":" + user.getId()) != null;
            }
            // likeCountMap 에 값이 있음 = 개수 출력, 없음 = 0
            Long likeCountLong = likeCountMap.get(commentId);
            int likeCount = likeCountLong == null ? 0 : likeCountLong.intValue();

            commentResponseDtoList.add(DictQuestionCommentResponseDto.builder()
                    .commentId(comment.getQuestionCommentId())
                    .commentWriterId(writerMap.get(commentId + ":username"))
                    .commentWriter(writerMap.get(commentId + ":nickname"))
                    .profileImageUrl(writerMap.get(commentId + ":profileImage"))
                    .commentContent(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .isLike(isLike)
                    .likeCount(likeCount)
                    .isSelected(commentId.equals(selectedComment))
                    .build());
        }

        return commentResponseDtoList;
    }

    private Long getSelectedComment(List<DictQuestionComment> commentList) {
        Optional<QuestionSelect> questionSelect = questionSelectRepository.findByQuestionCommentIn(commentList);

        if (questionSelect.isPresent()) {
            return questionSelect.get().getQuestionComment().getQuestionCommentId();
        } else {
            return 0L;
        }
    }

    private HashMap<Long, Long> getLikeCountMap(List<DictQuestionComment> commentList) {
        QQuestionCommentLike qQuestionCommentLike = QQuestionCommentLike.questionCommentLike;
        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        List<Tuple> likeCountTuple = queryFactory
                .select(qQuestionCommentLike.comment.questionCommentId, qQuestionCommentLike.user.count().as(count))
                .from(qQuestionCommentLike)
                .where(qQuestionCommentLike.comment.in(commentList)
                        .and(qQuestionCommentLike.comment.enabled.eq(true)))
                .groupBy(qQuestionCommentLike.comment.questionCommentId)
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(likeCountTuple);
    }

    private HashMap<String, Boolean> getLikeMap(List<DictQuestionComment> commentList) {
        QQuestionCommentLike qQuestionCommentLike = QQuestionCommentLike.questionCommentLike;
        List<Tuple> likeTuple = queryFactory
                .select(qQuestionCommentLike.comment.questionCommentId, qQuestionCommentLike.user.id)
                .from(qQuestionCommentLike)
                .where(qQuestionCommentLike.comment.in(commentList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(likeTuple);
    }

    private HashMap<String, String> getUserInfoMap(List<DictQuestionComment> commentList) {
        // 얻어오는 정보 : 사용자 아이디, 사용자 닉네임, 사용자 프로필이미지
        QDictQuestionComment qDictQuestionComment = QDictQuestionComment.dictQuestionComment;
        List<Tuple> userInfoTuple = queryFactory
                .select(qDictQuestionComment.questionCommentId, qDictQuestionComment.user.username, qDictQuestionComment.user.nickname, qDictQuestionComment.user.profileImage)
                .from(qDictQuestionComment)
                .where(qDictQuestionComment.in(commentList))
                .fetch();

        return MemegleServiceStaticMethods.getUserInfoMap(userInfoTuple);
    }
    // endregion
}
