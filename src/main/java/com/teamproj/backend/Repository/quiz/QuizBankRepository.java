package com.teamproj.backend.Repository.quiz;

import com.teamproj.backend.model.quiz.QuizBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizBankRepository extends JpaRepository<QuizBank, Long> {
}
