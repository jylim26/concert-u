package com.concertu.queue.infra;

public interface QueueRepository {

    void enqueue(Long userId, long score);

    Long findAheadCount(Long userId);

    Long countWaiting();

    boolean isActive(Long userId);

    long activateTopWaiting(long count);
}
