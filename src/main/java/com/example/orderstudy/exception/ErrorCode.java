package com.example.orderstudy.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    COUPON_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰 정책을 찾을 수 없습니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."),
    COUPON_POLICY_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "쿠폰 발급 기간이 아닙니다."),
    COUPON_SOLD_OUT(HttpStatus.CONFLICT, "쿠폰 수량이 모두 소진되었습니다."),
    DUPLICATED_COUPON_ISSUE(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    COUPON_OWNER_MISMATCH(HttpStatus.BAD_REQUEST, "쿠폰 소유자가 일치하지 않습니다."),
    COUPON_NOT_AVAILABLE(HttpStatus.CONFLICT, "쿠폰을 사용할 수 없는 상태입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "쿠폰이 만료되었습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 1 이상이어야 합니다."),
    ALREADY_CANCELED_ORDER(HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
