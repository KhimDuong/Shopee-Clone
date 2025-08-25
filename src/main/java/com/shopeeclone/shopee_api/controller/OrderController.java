package com.shopeeclone.shopee_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUserId(@PathVariable Long userId) throws Exception {
        return orderService.getOrdersByUserId(userId);
    }

    @PostMapping("/user/{userId}")
    public Order createOrder(@PathVariable Long userId, @RequestBody Order order) throws Exception {
        return orderService.createOrder(userId, order);
    }

    @DeleteMapping("/{id}") // Temporary
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }
}
