package com.teamproj.backend.dto.dict.mymeme;

import com.teamproj.backend.dto.youtube.DictRelatedYoutubeDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictMyMemeResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private List<DictRelatedYoutubeDto> relatedYoutube;
}
