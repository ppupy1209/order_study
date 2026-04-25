package com.example.orderstudy.controller.order;

import com.example.orderstudy.dto.order.CancelOrderResponse;
import com.example.orderstudy.dto.order.CreateOrderRequest;
import com.example.orderstudy.dto.order.CreateOrderResponse;
import com.example.orderstudy.dto.order.OrderResponse;
import com.example.orderstudy.service.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse findById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    @PatchMapping("/{orderId}/cancel")
    public CancelOrderResponse cancel(@PathVariable Long orderId) {
        return orderService.cancel(orderId);
    }
}
