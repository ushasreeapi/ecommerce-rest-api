
package com.ushasree.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalUsers;

    private Long totalProducts;

    private Long totalCategories;

    private Long totalOrders;

    private BigDecimal totalRevenue;

    private Map<String, Long> orderStatusSummary;

    private List<ProductResponse> lowStockProducts;

    private List<OrderResponse> recentOrders;
}

