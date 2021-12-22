package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.BoardSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardSubjectRepository extends JpaRepository<BoardSubject, Long> {
    Optional<BoardSubject> findBySubject(String subject);
}
