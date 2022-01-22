package com.teamproj.backend.model.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCategory {
    @Id
    private String categoryName;

    @OneToMany(mappedBy = "boardCategory", cascade = CascadeType.ALL)
    private final List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "boardCategory", cascade = CascadeType.ALL)
    private final List<BoardTodayLike> boardTodayLikeList = new ArrayList<>();
}
