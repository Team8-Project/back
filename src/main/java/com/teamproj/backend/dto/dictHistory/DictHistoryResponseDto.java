package com.teamproj.backend.dto.dictHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictHistoryResponseDto {
    private String dictName;
    private String author;
    private String prevContent;
}
