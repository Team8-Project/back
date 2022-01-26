package com.teamproj.backend.model.dict.question;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSelect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selectId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private DictQuestion dictQuestion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private DictQuestionComment questionComment;
}
