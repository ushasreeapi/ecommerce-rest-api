
package com.ushasree.ecommerce.repository;

import com.ushasree.ecommerce.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTrackingRepository
        extends JpaRepository<OrderTracking, Long> {

    List<OrderTracking> findByOrderIdOrderByUpdatedAtAsc(Long orderId);
}
