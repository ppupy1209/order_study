package com.example.orderstudy.controller.order;

import com.example.orderstudy.dto.order.OrderDtos;
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
    public OrderDtos.CreateOrderResponse create(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{orderId}")
    public OrderDtos.OrderResponse findById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderDtos.CancelOrderResponse cancel(@PathVariable Long orderId) {
        return orderService.cancel(orderId);
    }
}
