package com.teamproj.backend.dto.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictBestResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private boolean isLike;
    private int likeCount;
}
