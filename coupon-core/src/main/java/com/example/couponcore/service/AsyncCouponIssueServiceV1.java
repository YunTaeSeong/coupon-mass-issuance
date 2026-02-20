package com.example.couponcore.service;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.repository.redis.CouponIssueRequest;
import com.example.couponcore.repository.redis.RedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;
    private final RedisTemplate<Object, Object> redisTemplate;

    public void issue(long couponId, long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);

        // 날짜 검증
        if(!coupon.availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. couponId: %s, issueStart: %s, issueEnd: %s".formatted(couponId, coupon.getDateIssueStart(), coupon.getDateIssueEnd()));
        }

        // 동시성 이슈 제어
        distributeLockExecutor.execute("lock_%s".formatted(couponId), 3000, 3000, () -> {
            // 수량 조회 검증
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급 요청이 처리 되었습니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            // 중복 발급 여부
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            // 발급 요청
            issueRequest(couponId, userId);
        });
    }

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest issueRequestDto = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(issueRequestDto);

            // 요청 unique, 발급 제한 수량
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));

            // 쿠폰 발급 Queue 적재, sAdd랑 키, 값 사용 용도가 다름 겹치면 안됨
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequestDto));
        }

    }

//    public void issue(long couponId, long userId) {
//        // 1. 유저의 요청을 sorted set 적재
//        String key = "issue.request.sorted_set.couponId=%s".formatted(couponId);
//        redisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());
//
//        // 2. 유저의 요청 순서를 조회
//
//        // 3. 조회 결과를 선착순 조건과 비교
//
//        // 4. 쿠폰 발급 queue 적재
//    }
}
