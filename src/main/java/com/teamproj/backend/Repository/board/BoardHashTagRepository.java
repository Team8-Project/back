package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardHashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardHashTagRepository extends JpaRepository<BoardHashTag, String> {
    List<BoardHashTag> findByHashTagName(String query);

    @Transactional
    @Modifying
    @Query("delete from BoardHashTag h where h.board = :ids")
    void deleteAllByIdInQuery(Board ids);
}
