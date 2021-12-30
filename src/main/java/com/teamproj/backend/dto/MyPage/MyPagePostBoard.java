package com.teamproj.backend.dto.MyPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MyPagePostBoard {
    private Long postId;
    private String title;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int likeCount;
}
