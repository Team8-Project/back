package com.teamproj.backend.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDeleteResponseDto {
    private String result;
}
