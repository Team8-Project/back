package com.teamproj.backend.Repository.quiz;

import com.teamproj.backend.model.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
