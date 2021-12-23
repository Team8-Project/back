package com.teamproj.backend.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class BoardResponseDto {
    private Long postId;
    private String title;
    private String nickname;
    private String subject;
    private int views;
    private int likeCnt;
    private LocalDate createdAt;
}
