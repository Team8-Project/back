package com.teamproj.backend.dto.dict.mymeme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictMyMemeResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
}
