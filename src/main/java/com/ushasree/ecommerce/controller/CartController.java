
package com.ushasree.ecommerce.controller;

import com.ushasree.ecommerce.dto.CartResponse;
import com.ushasree.ecommerce.service.CartService;

import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add product to cart
    @PostMapping("/items/{productId}")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam @Positive Integer quantity) {

        return ResponseEntity.ok(
                cartService.addToCart(
                        authentication.getName(),
                        productId,
                        quantity
                )
        );
    }

    // View cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName())
        );
    }

    // Update product quantity
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam @Positive Integer quantity) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        authentication.getName(),
                        productId,
                        quantity
                )
        );
    }

    // Remove individual product
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            Authentication authentication,
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(
                        authentication.getName(),
                        productId
                )
        );
    }

    // Clear entire cart
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.clearCart(authentication.getName())
        );
    }
}

