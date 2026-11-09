
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.OrderItemResponse;
import com.ushasree.ecommerce.dto.OrderResponse;
import com.ushasree.ecommerce.entity.*;
import com.ushasree.ecommerce.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final OrderTrackingService orderTrackingService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,OrderTrackingService orderTrackingService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderTrackingService = orderTrackingService;
    }

    // 1. Place an order
    @Transactional
    public OrderResponse placeOrder(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cart not found"));

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            if (Boolean.FALSE.equals(product.getActive())) {
                throw new IllegalArgumentException(
                        "Product is not available: " + product.getName());
            }

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Deduct product stock
            product.setQuantity(
                    product.getQuantity() - cartItem.getQuantity()
            );

            productRepository.save(product);

            // Calculate item subtotal using current product price
            BigDecimal price = product.getPrice();

            BigDecimal subtotal = price.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            totalAmount = totalAmount.add(subtotal);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

// Save initial order tracking history
        orderTrackingService.saveTracking(
                savedOrder,
                OrderStatus.PLACED,
                "Order placed successfully"
        );

        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(savedOrder);
    }

    // 2. Get all orders belonging to logged-in user
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 3. Get a specific order
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Order order = orderRepository
                .findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        return mapToResponse(order);
    }

    // 4. Cancel an order and restore stock
    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Order order = orderRepository
                .findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PLACED
                && order.getStatus() != OrderStatus.CONFIRMED) {

            throw new IllegalArgumentException(
                    "This order cannot be cancelled");
        }

        // Restore stock for every order item
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setQuantity(
                    product.getQuantity() + item.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order cancelledOrder = orderRepository.save(order);

// Save cancellation tracking history
        orderTrackingService.saveTracking(
                cancelledOrder,
                OrderStatus.CANCELLED,
                "Order cancelled successfully"
        );

        return mapToResponse(cancelledOrder);
    }

    // 5. Admin: Get all orders
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // 6. Admin: Update order status
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found"));

        if (newStatus == null || newStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Use the cancellation operation to cancel an order");
        }

        boolean validTransition =
                (order.getStatus() == OrderStatus.PLACED
                        && newStatus == OrderStatus.CONFIRMED)
                        || (order.getStatus() == OrderStatus.CONFIRMED
                        && newStatus == OrderStatus.SHIPPED)
                        || (order.getStatus() == OrderStatus.SHIPPED
                        && newStatus == OrderStatus.DELIVERED);

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid order status transition");
        }

        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);

// Save tracking history
        orderTrackingService.saveTracking(
                updatedOrder,
                newStatus,
                "Order status updated to " + newStatus
        );

        return mapToResponse(updatedOrder);
    }

    // Convert Order entity into response DTO
    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}

