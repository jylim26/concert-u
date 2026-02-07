package com.concertu.queue.api;

import com.concertu.queue.domain.QueueStatus;

public record QueueTokenResponse(
        Long userId,
        QueueStatus status,
        Long aheadCount,
        Long totalWaitingCount
) {
}
