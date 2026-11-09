
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.OrderTrackingResponse;
import com.ushasree.ecommerce.entity.Order;
import com.ushasree.ecommerce.entity.OrderStatus;
import com.ushasree.ecommerce.entity.OrderTracking;
import com.ushasree.ecommerce.repository.OrderRepository;
import com.ushasree.ecommerce.repository.OrderTrackingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderTrackingService {

    private final OrderTrackingRepository trackingRepository;
    private final OrderRepository orderRepository;

    public OrderTrackingService(
            OrderTrackingRepository trackingRepository,
            OrderRepository orderRepository) {
        this.trackingRepository = trackingRepository;
        this.orderRepository = orderRepository;
    }

    // Save a new tracking record
    @Transactional
    public void saveTracking(
            Order order,
            OrderStatus status,
            String description) {

        OrderTracking tracking = OrderTracking.builder()
                .order(order)
                .status(status)
                .description(description)
                .build();

        trackingRepository.save(tracking);
    }

    // Get tracking history for a customer's order
    @Transactional(readOnly = true)
    public List<OrderTrackingResponse> getTrackingHistory(
            String email, Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException(
                    "You cannot access this order tracking information");
        }

        return trackingRepository
                .findByOrderIdOrderByUpdatedAtAsc(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderTrackingResponse mapToResponse(
            OrderTracking tracking) {

        return OrderTrackingResponse.builder()
                .trackingId(tracking.getId())
                .orderId(tracking.getOrder().getId())
                .status(tracking.getStatus())
                .description(tracking.getDescription())
                .updatedAt(tracking.getUpdatedAt())
                .build();
    }
}
