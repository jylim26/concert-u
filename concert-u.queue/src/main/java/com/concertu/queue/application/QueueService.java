package com.concertu.queue.application;

import com.concertu.queue.api.*;
import com.concertu.queue.domain.*;
import com.concertu.queue.domain.exception.*;
import com.concertu.queue.infra.*;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;

    public QueueTokenResponse issueToken(Long userId) {
        queueRepository.enqueue(userId, System.currentTimeMillis());
        return getPosition(userId);
    }

    public QueueTokenResponse getPosition(Long userId) {
        if (queueRepository.isActive(userId)) {
            Long totalWaitingCount = queueRepository.countWaiting();
            return new QueueTokenResponse(userId, QueueStatus.ACTIVE, 0L, totalWaitingCount);
        }
        Long aheadCount = queueRepository.findAheadCount(userId);
        if (aheadCount == null) {
            throw new QueueTokenNotFoundException();
        }
        Long totalWaitingCount = queueRepository.countWaiting();
        return new QueueTokenResponse(userId, QueueStatus.WAITING, aheadCount, totalWaitingCount);
    }

    public long activateTopWaiting(long count) {
        return queueRepository.activateTopWaiting(count);
    }
}
