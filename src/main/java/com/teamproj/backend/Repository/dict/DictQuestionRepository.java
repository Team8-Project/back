package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.dict.question.DictQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DictQuestionRepository extends JpaRepository<DictQuestion, Long> {
    Optional<Page<DictQuestion>> findAllByEnabledOrderByCreatedAtDesc(boolean enabled, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update DictQuestion d set d.views = d.views + 1 where d.questionId = :id")
    void updateView(Long id);

    @Query(value =
            "SELECT *" +
            "  FROM dict_question d" +
            " WHERE enabled = :enabled" +
            "   AND match(question_name, content) against(:q in natural language mode)" +
            " ORDER BY created_at desc" +
            " LIMIT :page, :size",
            nativeQuery = true)
    Optional<List<DictQuestion>> findAllByTitleAndContentByFullText(String q, boolean enabled, int page, int size);

    Long countByEnabled(boolean enabled);
}
