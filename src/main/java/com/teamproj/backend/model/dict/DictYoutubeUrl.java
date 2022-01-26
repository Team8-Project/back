package com.teamproj.backend.model.dict;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictYoutubeUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long urlId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Dict dict;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String youtubeUrl;

    @Column
    private String thumbNail;
}
