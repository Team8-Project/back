package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.BoardHashTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardHashTagRepository extends JpaRepository<BoardHashTag, String> {
}
