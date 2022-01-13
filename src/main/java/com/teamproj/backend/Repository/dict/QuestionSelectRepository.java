package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.DictQuestionComment;
import com.teamproj.backend.model.dict.question.QuestionSelect;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSelectRepository extends JpaRepository<QuestionSelect, Long> {
    boolean existsByDictQuestion(DictQuestion dictQuestion);
    boolean existsByQuestionComment(DictQuestionComment comment);
}
