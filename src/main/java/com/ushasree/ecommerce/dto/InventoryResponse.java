
package com.ushasree.ecommerce.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponse {

    private Long productId;

    private String productName;

    private Integer availableQuantity;

    private Boolean lowStock;

    private String message;
}
