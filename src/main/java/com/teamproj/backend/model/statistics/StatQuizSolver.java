package com.teamproj.backend.model.statistics;

import com.teamproj.backend.util.Timestamped;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatQuizSolver extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solverId;

    @Column(nullable = false)
    private String solverIp;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int score;
}
