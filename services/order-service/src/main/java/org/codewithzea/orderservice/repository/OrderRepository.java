package org.codewithzea.orderservice.repository;


import org.codewithzea.orderservice.model.Order;
import org.codewithzea.orderservice.model.OrderStatus;
import org.codewithzea.orderservice.service.RestaurantClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {


    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByRestaurantId(UUID restaurantId, Pageable pageable);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND (o.customerId = :userId)")
    Optional<Order> findByIdAndUserAccess(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    void updateOrderStatus(@Param("id") UUID id, @Param("status") OrderStatus status);
}
