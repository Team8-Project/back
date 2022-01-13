package com.teamproj.backend.model.dict.question;

import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.util.Timestamped;
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
public class DictQuestionComment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionCommentId;

    // 댓글이 달린 질문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private DictQuestion dictQuestion;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    // 댓글 내용
    @Column(nullable = false)
    private String content;

    @OneToOne(mappedBy = "questionComment", cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, unique = true)
    private QuestionSelect questionSelect;

    @Column(columnDefinition = "boolean default true")
    private boolean enabled;

    public void update(String content){
        this.content = content;
    }
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
}
