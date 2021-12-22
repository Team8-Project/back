package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<List<Board>> findAllByBoardCategory(BoardCategory boardCategory);

    @Modifying
    @Query("update Board b set b.views = b.views + 1 where b.postId = :id")
    int updateView(Long id);
}
