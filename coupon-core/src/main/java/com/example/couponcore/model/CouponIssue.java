package com.example.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_issues")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIssue extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "coupon_id")
    private Long couponId;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "date_issued")
    @CreatedDate
    private LocalDateTime dateIssued;

    @Column(name = "date_used")
    private LocalDateTime dateUsed;
}
