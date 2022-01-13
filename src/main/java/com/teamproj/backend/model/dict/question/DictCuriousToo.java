package com.teamproj.backend.model.dict.question;

import com.teamproj.backend.model.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictCuriousToo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long curiousTooId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private DictQuestion dictQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;
}
