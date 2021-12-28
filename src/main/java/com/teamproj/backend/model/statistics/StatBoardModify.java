package com.teamproj.backend.model.statistics;

import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.util.Timestamped;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatBoardModify extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modifyId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;
}
