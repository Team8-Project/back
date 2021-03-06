package com.teamproj.backend.dto.dict;

import com.teamproj.backend.dto.youtube.DictRelatedYoutubeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictDetailResponseDto {
    private Long dictId;
    private String title;
    private String summary;
    private String meaning;
    private String firstWriter;
    private String firstWriterProfileImage;
    private String recentWriter;
    private String recentWriterProfileImage;
    private boolean isLike;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<DictRelatedYoutubeDto> relatedYoutube;
}
