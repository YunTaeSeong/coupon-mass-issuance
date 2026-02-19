package com.example.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean zAdd(String key, String value, double score) {
        // 데이터가 없는 경우만 처리
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

}
