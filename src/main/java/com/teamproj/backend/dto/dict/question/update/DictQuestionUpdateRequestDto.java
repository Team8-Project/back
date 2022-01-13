package com.teamproj.backend.dto.dict.question.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DictQuestionUpdateRequestDto {
    private String title;
    private String content;
}
