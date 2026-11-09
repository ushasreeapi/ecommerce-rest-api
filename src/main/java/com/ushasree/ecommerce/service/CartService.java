
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.CartItemResponse;
import com.ushasree.ecommerce.dto.CartResponse;
import com.ushasree.ecommerce.entity.Cart;
import com.ushasree.ecommerce.entity.CartItem;
import com.ushasree.ecommerce.entity.Product;
import com.ushasree.ecommerce.entity.User;
import com.ushasree.ecommerce.exception.InsufficientStockException;
import com.ushasree.ecommerce.exception.ProductNotFoundException;
import com.ushasree.ecommerce.repository.CartItemRepository;
import com.ushasree.ecommerce.repository.CartRepository;
import com.ushasree.ecommerce.repository.ProductRepository;
import com.ushasree.ecommerce.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Add product to cart
    @Transactional
    public CartResponse addToCart(String email, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found"));

        if (Boolean.FALSE.equals(product.getActive())) {
            throw new IllegalArgumentException("Product is not available");
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        int updatedQuantity = quantity;

        if (cartItem != null) {
            updatedQuantity = cartItem.getQuantity() + quantity;
        }

        if (updatedQuantity > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity exceeds available stock");
        }

        if (cartItem == null) {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();
        } else {
            cartItem.setQuantity(updatedQuantity);
            cartItem.setPrice(product.getPrice());
        }

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    // View cart
    @Transactional
    public CartResponse getCart(String email) {

        Cart cart = getOrCreateCart(email);

        return buildCartResponse(cart);
    }

    // Update product quantity
    @Transactional
    public CartResponse updateCartItem(
            String email, Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found in cart"));

        Product product = cartItem.getProduct();

        if (quantity > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity exceeds available stock");
        }

        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice());

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    // Remove individual product from cart
    @Transactional
    public CartResponse removeFromCart(String email, Long productId) {

        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found in cart"));

        cartItemRepository.delete(cartItem);

        return buildCartResponse(cart);
    }

    // Clear entire cart
    @Transactional
    public CartResponse clearCart(String email) {

        Cart cart = getOrCreateCart(email);

        cartItemRepository.deleteByCartId(cart.getId());

        return buildCartResponse(cart);
    }

    // Find existing user or create their cart
    private Cart getOrCreateCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .build()
                ));
    }

    // Convert cart entity into response DTO
    private CartResponse buildCartResponse(Cart cart) {

        List<CartItemResponse> items = cartItemRepository
                .findByCartId(cart.getId())
                .stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }
}

