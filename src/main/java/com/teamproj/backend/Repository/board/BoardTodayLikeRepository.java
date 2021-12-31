package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.board.BoardTodayLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface BoardTodayLikeRepository extends JpaRepository<BoardTodayLike, Long> {

    Page<BoardTodayLike> findAllByBoardCategoryOrderByLikeCountDesc(BoardCategory boardCategory, Pageable pageable);

    Optional<BoardTodayLike> findByBoard(Board board);

    @Modifying
    @Transactional
    @Query("update BoardTodayLike b set b.likeCount = 0")
    void resetAll();
}
