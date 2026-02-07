package com.concertu.queue.infra;

import lombok.*;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepository implements QueueRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void enqueue(Long userId, long score) {
        redisTemplate.opsForZSet().add(QueueRedisKeys.WAITING_KEY, String.valueOf(userId), score);
    }

    @Override
    public Long findAheadCount(Long userId) {
        return redisTemplate.opsForZSet().rank(QueueRedisKeys.WAITING_KEY, String.valueOf(userId));
    }

    @Override
    public Long countWaiting() {
        return redisTemplate.opsForZSet().zCard(QueueRedisKeys.WAITING_KEY);
    }

    @Override
    public boolean isActive(Long userId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(QueueRedisKeys.ACTIVE_KEY, String.valueOf(userId));
        return Boolean.TRUE.equals(isMember);
    }

    @Override
    public long activateTopWaiting(long count) {
        if (count <= 0) {
            return 0L;
        }
        Set<String> users = redisTemplate.opsForZSet().range(QueueRedisKeys.WAITING_KEY, 0, count - 1);
        if (users == null || users.isEmpty()) {
            return 0L;
        }
        Long addedCount = redisTemplate.opsForSet().add(QueueRedisKeys.ACTIVE_KEY, users.toArray(new String[0]));
        redisTemplate.opsForZSet().remove(QueueRedisKeys.WAITING_KEY, users.toArray());
        return addedCount == null ? 0L : addedCount;
    }
}
