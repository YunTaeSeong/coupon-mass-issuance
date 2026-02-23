package com.example.couponcore.model;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, name = "coupon_type")
    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(nullable = false, name = "issued_quantity")
    private int issuedQuantity;

    @Column(nullable = false, name = "discount_amount")
    private int discountAmount;

    @Column(nullable = false, name = "min_available_amount")
    private int minAvailableQuantity;

    @Column(nullable = false, name = "date_issue_start")
    private LocalDateTime dateIssueStart;

    @Column(nullable = false, name = "date_issue_end")
    private LocalDateTime dateIssueEnd;

    // 수량 검증
    public boolean availableIssueQuantity() {
        if(totalQuantity == null) {
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    // 발급 기한
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    public void issue() {
        // 수량 검증 실패
        if(!availableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다. total : %s, issued : %s".formatted(totalQuantity, issuedQuantity));
        }

        // 기한 검증 실패
        if(!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. request : %s, issueStart : %s, issueEnd : %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
        issuedQuantity ++;
    }

    public boolean isIssueComplete() {
        LocalDateTime now = LocalDateTime.now();
        // dateIssueEnd 기간이 지났는지, 발급 가능 수량이 모두 소진되었는지
        return dateIssueEnd.isBefore(now) || !availableIssueQuantity();
    }

}
