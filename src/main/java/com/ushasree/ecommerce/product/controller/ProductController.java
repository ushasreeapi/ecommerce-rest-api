package com.ushasree.ecommerce.product.controller;

import com.ushasree.ecommerce.product.dto.ProductResponse;
import com.ushasree.ecommerce.product.entity.Product;
import com.ushasree.ecommerce.product.service.ProductService;
import org.springframework.web.bind.annotation.*;
import com.ushasree.ecommerce.product.dto.ProductRequest;
import jakarta.validation.Valid;
import java.util.List;
import com.ushasree.ecommerce.category.entity.Category;
import com.ushasree.ecommerce.category.repository.CategoryRepository;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService,CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            product.setCategory(category);
        }

        return mapToResponse(productService.createProduct(product));
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return mapToResponse(productService.getProductById(id));
    }



    @GetMapping
    public List<ProductResponse> getAllProducts() {

        return productService.getAllProducts()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            product.setCategory(category);
        }

        return mapToResponse(
                productService.updateProduct(id, product)
        );
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    private ProductResponse mapToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
        }

        return response;
    }
}
