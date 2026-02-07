package com.concertu.queue.api;

import com.concertu.queue.application.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/token")
    public QueueTokenResponse issueToken(@RequestHeader("X-User-Id") Long userId) {
        return queueService.issueToken(userId);
    }

    @GetMapping("/position")
    public QueueTokenResponse getPosition(@RequestHeader("X-User-Id") Long userId) {
        return queueService.getPosition(userId);
    }
}
