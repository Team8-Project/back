package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.model.dict.question.QuestionCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionCommentLikeRepository extends JpaRepository<QuestionCommentLike, Long> {
    boolean existsByUserAndComment(User user, DictQuestionComment comment);

    Optional<QuestionCommentLike> findByUserAndComment(User user, DictQuestionComment comment);

    void deleteByComment_QuestionCommentIdAndUser(Long commentId, User user);
}
