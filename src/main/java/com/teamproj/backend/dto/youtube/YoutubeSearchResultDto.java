package com.teamproj.backend.dto.youtube;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeSearchResultDto {
    private String title;
    private String channel;
    private String youtubeUrl;
}
