package com.example.couponcore.service;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponIssue;
import com.example.couponcore.model.event.CouponIssueCompleteEvent;
import com.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.couponcore.exception.ErrorCode.COUPON_NOT_EXISTS;
import static com.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

@RequiredArgsConstructor
@Service
public class CouponIssueService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * lock 획득
     *
     * 트랜잭션 시작
     * Coupon coupon = findCoupon(couponId);
     * coupon.issue();
     * saveCouponIssue(couponId, userId);
     * 트랜잭션 커밋
     *
     * lock 반납
     */
    @Transactional
    public void issue(long couponId, long userId) {
//        // redis lock
//        Coupon coupon = findCoupon(couponId);

        // mysql lock
        Coupon coupon = findCouponWithLock(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
        publishCouponEvent(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon findCoupon(long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() -> {
            throw new CouponIssueException(COUPON_NOT_EXISTS, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

    @Transactional(readOnly = true)
    public Coupon findCouponWithLock(long couponId) {
        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(() -> {
            throw new CouponIssueException(COUPON_NOT_EXISTS, "쿠폰 정책이 존재하지 않습니다. %s".formatted(couponId));
        });
    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();
        return couponIssueJpaRepository.save(couponIssue);
    }

    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다. user_id: %d, coupon_id: %d".formatted(userId, couponId));
        }
    }

    private void publishCouponEvent(Coupon coupon) {
        // 발급이 모두 된 경우(소진)
        if (coupon.isIssueComplete()) {
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }
}