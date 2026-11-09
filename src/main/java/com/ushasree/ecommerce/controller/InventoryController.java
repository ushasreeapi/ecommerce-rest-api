
package com.ushasree.ecommerce.controller;

import com.ushasree.ecommerce.dto.InventoryRequest;
import com.ushasree.ecommerce.dto.InventoryResponse;
import com.ushasree.ecommerce.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Add or Remove Stock
    @PutMapping("/{productId}/stock")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateStock(productId, request)
        );
    }

    // Get Available Stock
    @GetMapping("/{productId}/stock")
    public ResponseEntity<InventoryResponse> getAvailableStock(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getAvailableStock(productId)
        );
    }

    // Check Low Stock
    @GetMapping("/{productId}/low-stock")
    public ResponseEntity<InventoryResponse> checkLowStock(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") Integer threshold) {

        return ResponseEntity.ok(
                inventoryService.checkLowStock(productId, threshold)
        );
    }
}

