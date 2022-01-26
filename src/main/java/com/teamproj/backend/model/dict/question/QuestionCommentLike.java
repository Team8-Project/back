package com.teamproj.backend.model.dict.question;

import com.teamproj.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private DictQuestionComment comment;
}
