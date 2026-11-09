
package com.ushasree.ecommerce.service;

import com.ushasree.ecommerce.dto.CategoryRequest;
import com.ushasree.ecommerce.dto.CategoryResponse;
import com.ushasree.ecommerce.entity.Category;
import com.ushasree.ecommerce.repository.CategoryRepository;
import com.ushasree.ecommerce.exception.CategoryAlreadyExistsException;
import com.ushasree.ecommerce.exception.CategoryNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Create category
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException(
                    "Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    // Get all categories
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get category by ID
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found"));

        return mapToResponse(category);
    }

    // Update category
    public CategoryResponse updateCategory(
            Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found"));

        if (categoryRepository.existsByName(request.getName())
                && !category.getName().equalsIgnoreCase(request.getName())) {

            throw new CategoryAlreadyExistsException(
                    "Category already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    // Delete category
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found"));

        categoryRepository.delete(category);
    }

    // Convert Entity into Response DTO
    private CategoryResponse mapToResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}


