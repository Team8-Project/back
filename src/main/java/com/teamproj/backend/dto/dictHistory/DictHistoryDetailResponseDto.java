package com.teamproj.backend.dto.dictHistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictHistoryDetailResponseDto {
    private Long dictId;
    private Long revertFrom;
    private String title;
    private String firstWriter;
    private String modifier;
    private String summary;
    private String content;
    private LocalDate createdAt;
}
