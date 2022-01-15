package com.teamproj.backend.dto.dict.question.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictQuestionMyPageResponseDto {
    private Long questionId;
    private String thumbNail;
    private String title;
    private String content;
    private String username;
    private String profileImageUrl;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int curiousTooCnt;
    private int commentCnt;
    private Boolean isCuriousToo;
    private Boolean isComplete;
}
