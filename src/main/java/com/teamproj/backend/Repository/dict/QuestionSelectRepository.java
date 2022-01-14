package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.model.dict.question.QuestionSelect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionSelectRepository extends JpaRepository<QuestionSelect, Long> {
    boolean existsByDictQuestion(DictQuestion dictQuestion);

    boolean existsByQuestionComment(DictQuestionComment comment);

    Optional<QuestionSelect> findByQuestionCommentIn(List<DictQuestionComment> commentList);
}
