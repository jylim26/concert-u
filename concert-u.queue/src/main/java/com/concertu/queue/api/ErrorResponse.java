package com.concertu.queue.api;

public record ErrorResponse(
        int status,
        String message
) {
}
