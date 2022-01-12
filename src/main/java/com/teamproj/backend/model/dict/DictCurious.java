package com.teamproj.backend.model.dict;

import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictCurious extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictCuriousId;

    @Column(nullable = false)
    private String curiousName;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
