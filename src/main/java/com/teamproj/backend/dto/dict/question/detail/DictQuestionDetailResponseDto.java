package com.teamproj.backend.dto.dict.question.detail;

import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Service
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictQuestionDetailResponseDto {
    private Long questionId;
    private String title;
    private String username;
    private String content;
    private String writer;
    private String profileImageUrl;
    private String thumbNail;
    private LocalDateTime createdAt;
    private int views;
    private int curiousTooCnt;
    private Boolean isCuriousToo;
    private Long selectedComment;
    private List<DictQuestionCommentResponseDto> commentList;
    private int commentCnt;
}
