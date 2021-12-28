package com.teamproj.backend.model.statistics;

import com.teamproj.backend.util.Timestamped;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatQuizStarter extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long starterId;

    @Column(nullable = false)
    private String starterIp;

    @Column(nullable = false)
    private String type;
}
