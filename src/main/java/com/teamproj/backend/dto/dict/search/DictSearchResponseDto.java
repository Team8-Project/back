package com.teamproj.backend.dto.dict.search;

import com.teamproj.backend.dto.dict.DictSearchResultResponseDto;
import com.teamproj.backend.dto.dict.question.search.DictQuestionSearchResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@Service
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictSearchResponseDto {
    private List<DictSearchResultResponseDto> dictResult;
    private List<DictQuestionSearchResponseDto> questionResult;
}
