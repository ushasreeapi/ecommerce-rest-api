
package com.ushasree.ecommerce.repository;

import com.ushasree.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByActiveTrue();
    long count();

    List<Product> findByQuantityLessThanEqualAndActiveTrue(Integer threshold);
}
