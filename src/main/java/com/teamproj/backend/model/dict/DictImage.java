package com.teamproj.backend.model.dict;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class DictImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Dict dict;

    @Column(nullable = false)
    private String imageUrl;
}
