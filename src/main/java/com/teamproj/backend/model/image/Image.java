package com.teamproj.backend.model.image;

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
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardImageId;

    @Column(nullable = false)
    private ImageTypeEnum imageTypeEnum;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String imageUrl;
}
