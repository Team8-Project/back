package com.teamproj.backend.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentPostResponseDto {
    private Long commentId;
    private String profileImageUrl;
    private String commentWriterId;
    private String commentWriter;
    private String commentContent;
    private LocalDateTime createdAt;
}
