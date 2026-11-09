
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.InventoryRequest;
import com.ushasree.ecommerce.dto.InventoryResponse;
import com.ushasree.ecommerce.entity.Product;
import com.ushasree.ecommerce.exception.InsufficientStockException;
import com.ushasree.ecommerce.exception.ProductNotFoundException;
import com.ushasree.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse updateStock(Long productId, InventoryRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if ("IN".equals(request.getOperation())) {
            product.setQuantity(product.getQuantity() + request.getQuantity());

        } else if ("OUT".equals(request.getOperation())) {

            if (product.getQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock available");
            }

            product.setQuantity(product.getQuantity() - request.getQuantity());
        }

        Product updatedProduct = productRepository.save(product);

        return mapToResponse(
                updatedProduct,
                "IN".equals(request.getOperation())
                        ? "Stock added successfully"
                        : "Stock removed successfully",
                5
        );
    }

    public InventoryResponse getAvailableStock(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return mapToResponse(product, "Stock retrieved successfully", 5);
    }

    public InventoryResponse checkLowStock(Long productId, Integer threshold) {

        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return mapToResponse(
                product,
                "Stock status retrieved successfully",
                threshold
        );
    }

    private InventoryResponse mapToResponse(
            Product product, String message, Integer threshold) {

        return InventoryResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .availableQuantity(product.getQuantity())
                .lowStock(product.getQuantity() <= threshold)
                .message(message)
                .build();
    }
}
