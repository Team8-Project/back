package com.teamproj.backend.model.dict;

import com.teamproj.backend.model.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Dict dict;
}
