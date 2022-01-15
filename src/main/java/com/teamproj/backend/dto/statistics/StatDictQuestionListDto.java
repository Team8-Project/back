package com.teamproj.backend.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatDictQuestionListDto {
    private Long questionId;
    private String profileImage;
    private String title;
}
