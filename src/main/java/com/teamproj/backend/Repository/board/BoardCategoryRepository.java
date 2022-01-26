package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, String> {
}
