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
public class StatVisitor extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitId;

    @Column(nullable = false)
    private String visitorIp;

    @Column
    private String referer;
}
