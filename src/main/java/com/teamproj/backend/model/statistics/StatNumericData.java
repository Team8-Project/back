package com.teamproj.backend.model.statistics;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatNumericData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long data;
}
