package com.example.couponcore.service;

import com.example.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.stereotype.Service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;
    private final AopInfrastructureBean aopInfrastructureBean;

    // 중복 발급 검증
    public boolean availableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        // 존재 하지 않는 대상만 true
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }

    // 수량 검증
    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        // 검증 스킵
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

}
