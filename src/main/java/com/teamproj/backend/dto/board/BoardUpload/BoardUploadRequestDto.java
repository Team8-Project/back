package com.teamproj.backend.dto.board.BoardUpload;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardUploadRequestDto {
    private String title;
    private String content;
}