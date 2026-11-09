
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.DashboardResponse;
import com.ushasree.ecommerce.dto.OrderResponse;
import com.ushasree.ecommerce.dto.ProductResponse;
import com.ushasree.ecommerce.entity.Order;
import com.ushasree.ecommerce.entity.OrderStatus;
import com.ushasree.ecommerce.entity.PaymentStatus;
import com.ushasree.ecommerce.entity.Product;
import com.ushasree.ecommerce.repository.CategoryRepository;
import com.ushasree.ecommerce.repository.OrderRepository;
import com.ushasree.ecommerce.repository.PaymentRepository;
import com.ushasree.ecommerce.repository.ProductRepository;
import com.ushasree.ecommerce.repository.UserRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(
            UserRepository userRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {

        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        Long totalUsers = userRepository.count();

        Long totalProducts = productRepository.count();

        Long totalCategories = categoryRepository.count();

        Long totalOrders = orderRepository.count();

        BigDecimal totalRevenue =
                paymentRepository.calculateTotalRevenueByStatus(
                        PaymentStatus.SUCCESS
                );

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Map<String, Long> orderStatusSummary =
                getOrderStatusSummary();

        List<ProductResponse> lowStockProducts =
                getLowStockProducts();

        List<OrderResponse> recentOrders =
                getRecentOrders();

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .orderStatusSummary(orderStatusSummary)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrders)
                .build();
    }

    private Map<String, Long> getOrderStatusSummary() {

        Map<String, Long> summary = new java.util.LinkedHashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            summary.put(status.name(), 0L);
        }

        List<Object[]> results = orderRepository.countOrdersByStatus();

        for (Object[] result : results) {
            OrderStatus status = (OrderStatus) result[0];
            Long count = (Long) result[1];

            summary.put(status.name(), count);
        }

        return summary;
    }

    private List<ProductResponse> getLowStockProducts() {

        List<Product> products =
                productRepository
                        .findByQuantityLessThanEqualAndActiveTrue(5);

        return products.stream()
                .map(this::mapProductToResponse)
                .toList();
    }

    private List<OrderResponse> getRecentOrders() {

        List<Order> orders =
                orderRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(0, 5)
                );

        return orders.stream()
                .map(this::mapOrderToResponse)
                .toList();
    }

    private ProductResponse mapProductToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .active(product.getActive())
                .categoryId(
                        product.getCategory() != null
                                ? product.getCategory().getId()
                                : null
                )
                .categoryName(
                        product.getCategory() != null
                                ? product.getCategory().getName()
                                : null
                )
                .createdAt(product.getCreatedAt())
                .build();
    }

    private OrderResponse mapOrderToResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(order.getItems().stream()
                        .map(item ->
                                com.ushasree.ecommerce.dto.OrderItemResponse
                                        .builder()
                                        .orderItemId(item.getId())
                                        .productId(item.getProduct().getId())
                                        .productName(item.getProductName())
                                        .quantity(item.getQuantity())
                                        .price(item.getPrice())
                                        .subtotal(item.getSubtotal())
                                        .build()
                        )
                        .toList())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
