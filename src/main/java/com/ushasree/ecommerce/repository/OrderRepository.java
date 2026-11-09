
package com.ushasree.ecommerce.repository;

import com.ushasree.ecommerce.entity.Order;
import com.ushasree.ecommerce.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    long count();

    @Query("""
    SELECT o.status, COUNT(o)
    FROM Order o
    GROUP BY o.status
""")
    List<Object[]> countOrdersByStatus();

    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

