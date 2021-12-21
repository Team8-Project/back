package com.teamproj.backend.model.dict;


import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.*;

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
    private User user;

    @Column(nullable = false, unique = true)
    private String dictName;

    @Column(nullable = false)
    private String content;

    @OneToMany(mappedBy = "dict")
    private final List<DictHistory> dictHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "dict")
    private final List<DictImage> dictImage = new ArrayList<>();
}
