
package com.ushasree.ecommerce.controller;

import com.ushasree.ecommerce.dto.PaymentRequest;
import com.ushasree.ecommerce.dto.PaymentResponse;
import com.ushasree.ecommerce.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Process payment for an order
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.processPayment(
                authentication.getName(), request);

        return ResponseEntity.ok(response);
    }

    // Get payment details by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            Authentication authentication,
            @PathVariable Long orderId) {

        PaymentResponse response = paymentService.getPaymentByOrderId(
                authentication.getName(), orderId);

        return ResponseEntity.ok(response);
    }
}
