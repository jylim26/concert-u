package com.concertu.queue.api;

import com.concertu.queue.domain.exception.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class QueueExceptionHandler {

    @ExceptionHandler(QueueTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQueueTokenNotFound(QueueTokenNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }
}
