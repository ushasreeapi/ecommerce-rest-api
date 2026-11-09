
package com.ushasree.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import com.ushasree.ecommerce.entity.OrderStatus;

@Getter
@Setter
public class OrderTrackingRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @NotBlank(message = "Description is required")
    private String description;
}
