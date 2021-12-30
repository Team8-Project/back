package com.teamproj.backend.dto.board.BoardUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardUpdateRequestDto {
    private String title;
    private String content;
    private List<String> hashTags;
}
