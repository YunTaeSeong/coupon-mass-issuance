package com.example.couponapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // NULL 일 경우 comment 안 보냄
public record CouponIssueResponseDto(boolean isSuccess, String comment) {
}
