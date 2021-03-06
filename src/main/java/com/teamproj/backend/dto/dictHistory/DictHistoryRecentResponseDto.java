package com.teamproj.backend.dto.dictHistory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictHistoryRecentResponseDto {
    private Long historyId;
    private String writerProfileImage;
    private Long revertFrom;
    private String writer;
    private LocalDateTime createdAt;
}
