package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long>  {
    List<BoardLike> findAllByBoard(Board board);
}
