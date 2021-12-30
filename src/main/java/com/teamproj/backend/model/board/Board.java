package com.teamproj.backend.model.board;

import com.teamproj.backend.dto.board.BoardUpdate.BoardUpdateRequestDto;
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
    private Long boardId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @ColumnDefault("0")
    private int views;

    @Column(nullable = false)
    private String thumbNail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private BoardCategory boardCategory;


    @Column(columnDefinition = "boolean default true")
    private boolean enabled;

    @OneToMany(mappedBy = "board")
    private List<BoardHashTag> boardHashTagList = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private final List<BoardLike> Likes = new ArrayList<>();

    public void update(BoardUpdateRequestDto boardUpdateRequestDto, String imageUrl) {
        this.title = boardUpdateRequestDto.getTitle();
        this.content = boardUpdateRequestDto.getContent();

        if (!imageUrl.isEmpty()) {
            this.thumbNail = imageUrl;
        }
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public void setHashTagList(List<BoardHashTag> boardHashTagList) {
        this.boardHashTagList = boardHashTagList;
    }
}