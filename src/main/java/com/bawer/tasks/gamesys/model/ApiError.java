package com.bawer.tasks.gamesys.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@Getter
public class ApiError {
    private HttpStatus status;
    private String message;
    private String developerMessage;
}
