package com.example.couponcore.service;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    // 캐시 대체
    public void checkCouponIssueQuantity(CouponRedisEntity coupon, long userId) {
        // 수량 조회 검증
        if (!availableTotalIssueQuantity(coupon.totalQuantity(), coupon.id())) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId: %s, userId: %s".formatted(coupon.id(), userId));
        }

        // 중복 발급 여부
        if (!availableUserIssueQuantity(coupon.id(), userId)) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리 되었습니다. couponId: %s, userId: %s".formatted(coupon.id(), userId));
        }
    }

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
