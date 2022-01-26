package com.teamproj.backend.dto.statistics;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatDictPostByDayDto {
    private String date;
    private Long count;
}
