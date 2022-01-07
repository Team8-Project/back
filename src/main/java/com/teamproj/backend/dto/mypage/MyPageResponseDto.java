package com.teamproj.backend.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyPageResponseDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private int postCount;
    private int dictCount;
    private List<MyPagePostBoardResponseDto> postBoards;
    private List<MyPageDictResponseDto> dict;
}
