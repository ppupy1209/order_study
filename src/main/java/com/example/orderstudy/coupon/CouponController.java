package com.example.orderstudy.coupon;

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
    public CouponDtos.CouponPolicyResponse createPolicy(@Valid @RequestBody CouponDtos.CreateCouponPolicyRequest request) {
        return couponService.createPolicy(request);
    }

    @PostMapping("/api/coupons/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponDtos.IssueCouponResponse issue(@Valid @RequestBody CouponDtos.IssueCouponRequest request) {
        return couponService.issue(request);
    }

    @GetMapping("/api/users/{userId}/coupons")
    public List<CouponDtos.UserCouponResponse> findUserCoupons(@PathVariable Long userId) {
        return couponService.findUserCoupons(userId);
    }
}
