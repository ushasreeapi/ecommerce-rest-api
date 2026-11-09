
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.PaymentRequest;
import com.ushasree.ecommerce.dto.PaymentResponse;
import com.ushasree.ecommerce.entity.*;
import com.ushasree.ecommerce.repository.OrderRepository;
import com.ushasree.ecommerce.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackingService orderTrackingService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,OrderTrackingService orderTrackingService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderTrackingService = orderTrackingService;
    }

    @Transactional
    public PaymentResponse processPayment(String email, PaymentRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Verify order ownership
        if (!order.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("You cannot pay for this order");
        }

        // Check whether payment already exists
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new IllegalArgumentException(
                    "Payment already exists for this order");
        }

        // Only placed orders can be paid
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalArgumentException(
                    "Payment is allowed only for placed orders");
        }

        // Simulate payment
        PaymentStatus paymentStatus = PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(paymentStatus)
                .transactionId("TXN-" + UUID.randomUUID())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

// Save order tracking history
        orderTrackingService.saveTracking(
                order,
                OrderStatus.CONFIRMED,
                "Payment successful. Order confirmed."
        );

        return mapToResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String email, Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found"));

        if (!payment.getOrder().getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException(
                    "You cannot access this payment");
        }

        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

