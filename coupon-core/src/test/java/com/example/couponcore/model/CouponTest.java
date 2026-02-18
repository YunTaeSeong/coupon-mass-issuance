package com.example.couponcore.model;

import com.example.couponcore.exception.CouponIssueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.example.couponcore.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    @DisplayName("발급 수량 남아있으면 true 반환")
    void availableIssueQuantity_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 수량 소진되었다면 false 반환")
    void availableIssueQuantity_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("최대 발급 수량 설정되지 않았다면 true 반환")
    void availableIssueQuantity_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();

        // when
        boolean result = coupon.availableIssueQuantity();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 시작되지 않았다면 false 반환")
    void availableIssueDate_1() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("발급 기간이 시작되었다면 true 반환")
    void availableIssueDate_2() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("발급 기간이 종료되면 false 반환")
    void availableIssueDate_3() {
        // given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        // when
        boolean result = coupon.availableIssueDate();

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("수량과 발급 기간이 유효하다면 발급에 성공")
    void issue_1() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when
        coupon.issue();

        // then
        assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    @DisplayName("발급 수량을 초과하면 예외를 반환")
    void issue_2() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> coupon.issue());
        assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 기간이 아니면 예외를 반환")
    void issue_3() {
        // given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        // when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> coupon.issue());
        assertEquals(exception.getErrorCode(), INVALID_COUPON_ISSUE_DATE);

    }
}