package com.lemon.lemonade.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Short-lived key/value storage in Redis. */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void addItem(String key, String value, Duration expiration) {
        redisTemplate.opsForValue().set(key, value, expiration);
    }

    public String getItem(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void removeItem(String key) {
        redisTemplate.delete(key);
    }

    /** Counts up, starting the expiry when the key is first created. */
    public long increment(String key, Duration expiration) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, expiration);
        }
        return count == null ? 0 : count;
    }
}
