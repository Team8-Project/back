package com.teamproj.backend.Repository.board;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardViewers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardViewersRepository extends JpaRepository<BoardViewers, Long> {
    Optional<BoardViewers> findByViewerIpAndBoard(String ip, Board board);
}
