package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    Long countByBoardCategoryAndEnabled(BoardCategory boardCategory, boolean enabled);

    Optional<Page<Board>> findAllByBoardCategoryAndEnabledOrderByCreatedAtDesc(BoardCategory boardCategory, boolean enabled, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update Board b set b.views = b.views + 1 where b.boardId = :id")
    void updateView(Long id);

    @Query(value = "SELECT *" +
                   "  FROM board b" +
                   " WHERE enabled = :enabled" +
                   "   AND b.board_category_category_name = :category" +
                   "   AND match(title, content) against(:q in natural language mode)" +
                   " ORDER BY created_at desc" +
                   " LIMIT :page, :size",
            nativeQuery = true)
    Optional<List<Board>> findAllByTitleAndContentByFullText(@Param("q") String query,
                                                             @Param("category") String category,
                                                             @Param("enabled") boolean enabled,
                                                             @Param("page") int page,
                                                             @Param("size") int size);
}