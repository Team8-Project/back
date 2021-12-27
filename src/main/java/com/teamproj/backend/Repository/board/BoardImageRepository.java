package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardImageRepository extends JpaRepository<BoardImage, String> {
}
