package com.concertu.queue.application;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueActivationScheduler {

    private final QueueService queueService;

    @Value("${queue.activate.batch-size:50}")
    private long batchSize;

    @Scheduled(fixedDelayString = "${queue.activate.fixed-delay-ms:1000}")
    public void activateTopWaiting() {
        long activatedCount = queueService.activateTopWaiting(batchSize);
        if (activatedCount > 0) {
            log.info("queue activation executed. batchSize={}, activatedCount={}", batchSize, activatedCount);
        }
    }
}
