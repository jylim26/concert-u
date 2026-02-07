package com.concertu.queue.infra;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueRedisKeys {

    public static final String WAITING_KEY = "queue:waiting";
    public static final String ACTIVE_KEY = "queue:active";
}
