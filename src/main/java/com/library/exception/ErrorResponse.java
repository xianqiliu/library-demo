package com.library.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class ErrorResponse
{
    public ErrorResponse(int code, String message, List<String> details) {
        super();
        this.code = code;
        this.message = message;
        this.details = details;
    }

    private int code;

    private String message;

    private List<String> details;
}
