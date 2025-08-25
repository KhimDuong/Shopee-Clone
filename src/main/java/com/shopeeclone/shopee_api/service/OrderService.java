package com.shopeeclone.shopee_api.service;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopeeclone.shopee_api.model.Order;
import com.shopeeclone.shopee_api.model.User;
import com.shopeeclone.shopee_api.repository.OrderRepository;
import com.shopeeclone.shopee_api.repository.UserRepository;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final SecureOrderService secureOrderService;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, SecureOrderService secureOrderService, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.secureOrderService = secureOrderService;
        this.userRepository = userRepository;
    }

    private Long currentUserId(Principal principal) {
        String username = principal.getName();
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return u.getId();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(Principal principal) throws Exception {
        Long userId = currentUserId(principal);
        List<Order> orders = orderRepository.findByUserId(userId);

        for (Order order : orders) {
            String decryptedDetails = secureOrderService.loadOrderDetails(userId, order.getEncryptedDetails());
            order.setDetailsJson(decryptedDetails);
            order.setEncryptedDetails(null);
        }

        return orders;
    }

    public Order createOrder(Principal principal, Order order) throws Exception {
        return secureOrderService.saveOrder(currentUserId(principal), order);
    }

    public String getDecryptedOrderDetails(Long clientId, Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return secureOrderService.loadOrderDetails(clientId, order.getEncryptedDetails());
    }
}
