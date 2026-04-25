package com.example.orderstudy.controller.coupon;

import com.example.orderstudy.dto.coupon.CouponPolicyResponse;
import com.example.orderstudy.dto.coupon.CreateCouponPolicyRequest;
import com.example.orderstudy.dto.coupon.IssueCouponRequest;
import com.example.orderstudy.dto.coupon.IssueCouponResponse;
import com.example.orderstudy.dto.coupon.UserCouponResponse;
import com.example.orderstudy.service.coupon.CouponService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/api/coupon-policies")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponPolicyResponse createPolicy(@Valid @RequestBody CreateCouponPolicyRequest request) {
        return couponService.createPolicy(request);
    }

    @PostMapping("/api/coupons/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueCouponResponse issue(@Valid @RequestBody IssueCouponRequest request) {
        return couponService.issue(request);
    }

    @GetMapping("/api/users/{userId}/coupons")
    public List<UserCouponResponse> findUserCoupons(@PathVariable Long userId) {
        return couponService.findUserCoupons(userId);
    }
}
