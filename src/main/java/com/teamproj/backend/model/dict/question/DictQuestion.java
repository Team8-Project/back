package com.teamproj.backend.model.dict.question;

import com.teamproj.backend.dto.dict.question.update.DictQuestionUpdateRequestDto;
import com.teamproj.backend.model.Comment;
import com.teamproj.backend.model.User;
import com.teamproj.backend.util.Timestamped;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictQuestion extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false)
    private String questionName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String thumbNail;

    @ColumnDefault("0")
    private int views;

    @ColumnDefault("1")
    private boolean enabled;

    @OneToOne(mappedBy = "dictQuestion", cascade = CascadeType.ALL)
    @JoinColumn(unique = true)
    private QuestionSelect questionSelect;

    @OneToMany(mappedBy = "dictQuestion")
    private final List<DictQuestionComment> questionCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "dictQuestion")
    private final List<DictCuriousToo> dictCuriousTooList = new ArrayList<>();

    public void update(DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto,String imageUrl){
        this.questionName = dictQuestionUpdateRequestDto.getTitle();
        this.content = dictQuestionUpdateRequestDto.getContent();
        this.thumbNail = imageUrl;
    }
}
