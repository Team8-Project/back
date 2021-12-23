package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long>  {
    List<BoardLike> findAllByBoard(Board board);
    Optional<BoardLike> findByBoardAndUser(Board board, User user);
}
