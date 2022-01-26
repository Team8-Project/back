package com.teamproj.backend.dto.dict.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DictQuestionUploadResponseDto {
    private Long questionId;
    private String title;
    private String content;
    private String thumbNail;
    private LocalDateTime createdAt;
}
