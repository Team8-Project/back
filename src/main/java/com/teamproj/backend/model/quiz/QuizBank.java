package com.teamproj.backend.model.quiz;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizBankId;

    // 어디 문제의 선택지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String choice;
}
