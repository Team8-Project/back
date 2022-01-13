package com.teamproj.backend.Repository.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.DictCuriousToo;
import com.teamproj.backend.model.dict.question.DictQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictCuriousTooRepository extends JpaRepository<DictCuriousToo, Long> {
    boolean existsByUserAndDictQuestion(User user, DictQuestion dictQuestion);

    Optional<DictCuriousToo> findByDictQuestionAndUser(DictQuestion dictQuestion, User user);

    void deleteByDictQuestionAndUser(DictQuestion dictQuestion, User user);
}
