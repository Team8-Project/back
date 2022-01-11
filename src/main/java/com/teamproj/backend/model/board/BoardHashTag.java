package com.teamproj.backend.model.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardHashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashTagId;

    @Column(nullable = false)
    private String hashTagName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Board board;
}
