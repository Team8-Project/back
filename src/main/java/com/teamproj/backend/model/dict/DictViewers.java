package com.teamproj.backend.model.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictViewers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long viewersId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Dict dict;

    @Column(nullable = false)
    private String viewerIp;
}
