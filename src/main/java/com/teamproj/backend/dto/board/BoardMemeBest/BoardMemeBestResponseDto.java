package com.teamproj.backend.dto.board.BoardMemeBest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BoardMemeBestResponseDto {
    private Long boardId;
    private String thumbNail;
    private String title;
    private String username;
    private String profileImageUrl;
    private String writer;
    private String content;
    private int views;
    private Long likeCnt;
    private Boolean isLike;

    public void updateIsLike(Boolean isLike) {
        this.isLike = isLike;
    }
}
