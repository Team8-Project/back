//package com.teamproj.backend.model;
//
//import com.teamproj.backend.model.board.Board;
//import com.teamproj.backend.model.dict.question.DictQuestion;
//import com.teamproj.backend.model.dict.question.QuestionSelect;
//import com.teamproj.backend.util.Timestamped;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//
//@Entity
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Comment extends Timestamped {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long commentId;
//
//    // 댓글이 달린 게시글
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = true)
//    private Board board;
//
//    // 댓글 작성자
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false)
//    private User user;
//
//    // 댓글 내용
//    @Column(nullable = false)
//    private String content;
//
//    @Column(columnDefinition = "boolean default true")
//    private boolean enabled;
//
//    public void update(String content){
//        this.content = content;
//    }
//    public void setEnabled(boolean enabled){
//        this.enabled = enabled;
//    }
//}
