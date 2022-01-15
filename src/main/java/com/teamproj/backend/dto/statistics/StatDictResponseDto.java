package com.teamproj.backend.dto.statistics;

import com.teamproj.backend.dto.rank.RankResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatDictResponseDto {
    private Long dictCountAll;
    private List<StatDictPostByDayDto> dictCountWeeks;
    private Long questionCountAll;
    private Long completeQuestionCountAll;
    private List<StatDictQuestionListDto> completeQuestionList;
    private Long remainQuestionCountAll;
    private List<StatDictQuestionListDto> remainQuestionList;
    private List<RankResponseDto> dictPostRank;
}
