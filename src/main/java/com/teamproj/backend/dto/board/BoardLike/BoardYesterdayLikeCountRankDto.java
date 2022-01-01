package com.teamproj.backend.dto.board.BoardLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BoardYesterdayLikeCountRankDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String nickname;
    private Long likeCnt;
}
