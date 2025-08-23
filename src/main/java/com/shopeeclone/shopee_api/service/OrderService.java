package com.shopeeclone.shopee_api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SecureOrderService secureOrderService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUserId(Long userId) throws Exception {
        List<Order> orders = orderRepository.findByUserId(userId);

        for (Order order : orders) {
            String decryptedDetails = secureOrderService.loadOrderDetails(userId, order.getEncryptedDetails());
            order.setDetailsJson(decryptedDetails); // Assume `setDetails` stores plain details in the entity for API response
        }

        return orders;
    }

    public Order createOrder(Long userId, Order order) throws Exception {
        return secureOrderService.saveOrder(userId, order);
    }

    public void cancelOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public String getDecryptedOrderDetails(Long clientId, Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return secureOrderService.loadOrderDetails(clientId, order.getEncryptedDetails());
    }
}
