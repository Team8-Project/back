package com.teamproj.backend.dto.dictCurious;

import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Setter
@Service
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictCuriousResponseDto {
    private List<Long> curiousId;
    private String curiousName;
    private String firstRequester;
}
