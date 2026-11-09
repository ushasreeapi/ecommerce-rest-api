
package com.ushasree.ecommerce.controller;

import com.ushasree.ecommerce.dto.OrderTrackingResponse;
import com.ushasree.ecommerce.service.OrderTrackingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-tracking")
public class OrderTrackingController {

    private final OrderTrackingService trackingService;

    public OrderTrackingController(
            OrderTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    // Get tracking history for an order
    @GetMapping("/{orderId}")
    public ResponseEntity<List<OrderTrackingResponse>> getTrackingHistory(
            Authentication authentication,
            @PathVariable Long orderId) {

        List<OrderTrackingResponse> response =
                trackingService.getTrackingHistory(
                        authentication.getName(), orderId);

        return ResponseEntity.ok(response);
    }
}

