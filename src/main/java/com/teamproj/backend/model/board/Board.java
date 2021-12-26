package com.teamproj.backend.model.board;

import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ColumnDefault("0")
    private int views;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BoardCategory boardCategory;

    @OneToMany(mappedBy = "board")
    private final List<BoardHashTag> boardHashTagList = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private final List<BoardLike> Likes = new ArrayList<>();

    public void update(BoardUploadRequestDto boardUploadRequestDto) {
        this.title = boardUploadRequestDto.getTitle();
        this.content = boardUploadRequestDto.getContent();
    }
}