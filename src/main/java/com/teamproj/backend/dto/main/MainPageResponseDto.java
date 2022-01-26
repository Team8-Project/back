package com.teamproj.backend.dto.main;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MainPageResponseDto {
    private String username;
    private String nickname;
    private List<MainTodayMemeResponseDto> todayMemes;
    private List<MainTodayBoardResponseDto> popularBoards;
    private List<MainMemeImageResponseDto> popularImages;
}
