package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BoardResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String username;
    private String writer;
    private String content;
    private LocalDate createdAt;
    private int views;
    private int likeCnt;
    private List<String> hashTags;
}
