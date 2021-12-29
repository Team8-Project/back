package com.teamproj.backend.model.dict;


import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dict extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User firstAuthor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User recentModifier;

    @Column(nullable = false, unique = true)
    private String dictName;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String content;

    @ColumnDefault("0")
    private int views;

    @OneToMany(mappedBy = "dict", cascade = CascadeType.ALL)
    private final List<DictHistory> dictHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "dict", cascade = CascadeType.ALL)
    private final List<DictImage> dictImageList = new ArrayList<>();

    @OneToMany(mappedBy = "dict", cascade = CascadeType.ALL)
    private final List<DictLike> dictLikeList = new ArrayList<>();
}
