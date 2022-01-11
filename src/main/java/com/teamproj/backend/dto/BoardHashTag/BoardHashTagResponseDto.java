package com.teamproj.backend.dto.BoardHashTag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardHashTagResponseDto {
    private List<String> hashTags;
}
