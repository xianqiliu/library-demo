package com.library.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(InternalServerException.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(500, "Server Error", details);
        ResponseEntity<Object> re = new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        logger.error(re.toString());
        return re;
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(404, "Record Not Found", details);
        ResponseEntity<Object> re = new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        logger.error(re.toString());
        return re;
    }

    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(400, "Bad Request", details);
        ResponseEntity<Object> re = new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        logger.error(re.toString());
        return re;
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<Object> handleConflictException(ConflictException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(409, "Conflict", details);
        ResponseEntity<Object> re = new ResponseEntity<>(error, HttpStatus.CONFLICT);
        logger.error(re.toString());
        return re;
    }

    @ExceptionHandler(NoContentException.class)
    public final ResponseEntity<Object> handleNoContentException(NoContentException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ErrorResponse error = new ErrorResponse(204, "NO CONTENT", details);
        ResponseEntity<Object> re = new ResponseEntity<>(error, HttpStatus.NO_CONTENT);
        logger.error(re.toString());
        return re;
    }
}
