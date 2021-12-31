package com.teamproj.backend.model.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardTodayLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardTodayLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BoardCategory boardCategory;

    @ColumnDefault("1")
    private Long likeCount;

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}
