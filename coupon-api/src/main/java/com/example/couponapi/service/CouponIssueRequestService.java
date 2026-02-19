package com.example.couponapi.service;

import com.example.couponapi.dto.CouponIssueRequestDto;
import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;

    private final Logger log = LoggerFactory.getLogger(CouponIssueRequestService.class);

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
////        synchronized ()
//        synchronized (this) {
//            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        }

//        // redis lock
//        distributeLockExecutor.execute("lock_" + requestDto.couponId(), 10000, 10000, () -> {
//            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        });

        // mysql lock
        couponIssueService.issue(requestDto.couponId(), requestDto.userId());

        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }
}
