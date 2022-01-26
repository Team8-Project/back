package com.teamproj.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
public class RestApiException {
    private String errorMessage;
    private HttpStatus httpStatus;
}
