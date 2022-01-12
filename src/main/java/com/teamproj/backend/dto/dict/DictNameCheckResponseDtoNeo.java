package com.teamproj.backend.dto.dict;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DictNameCheckResponseDtoNeo {
    private Long dictId;
    private String dictName;
    private String meaning;
    private boolean result;
}
