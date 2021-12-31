package com.teamproj.backend.dto.MyPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyPagePostBoard {
    private Long boardId;
    private String title;
    private String category;
    private String writer;
    private LocalDateTime createdAt;
    private int views;
    private int likeCount;
    private List<String> hashTags;
}
