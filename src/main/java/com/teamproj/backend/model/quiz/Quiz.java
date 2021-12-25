package com.teamproj.backend.model.quiz;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    // 정답
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn
    private QuizBank solution;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizBank> choiceList = new ArrayList<>();

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String category;
}
