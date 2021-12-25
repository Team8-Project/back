package com.teamproj.backend.model.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardViewers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viewersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Board board;

    @Column(nullable = false)
    private String viewerIp;
}
