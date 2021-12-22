package com.teamproj.backend.Repository;

import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBoard(Board board);
}
