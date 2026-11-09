
package com.ushasree.ecommerce.dto;

import com.ushasree.ecommerce.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderTrackingResponse {

    private Long trackingId;
    private Long orderId;
    private OrderStatus status;
    private String description;
    private LocalDateTime updatedAt;
}

