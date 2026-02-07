package com.concertu.queue.domain.exception;

public class QueueTokenNotFoundException extends RuntimeException {

    public QueueTokenNotFoundException() {
        super("Queue token not found");
    }
}
