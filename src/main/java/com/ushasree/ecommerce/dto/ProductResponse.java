
package com.ushasree.ecommerce.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private Boolean active;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime createdAt;
}
