package org.codewithzea.restaurantservice.repository;



import org.codewithzea.restaurantservice.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @EntityGraph(attributePaths = {"restaurant"})
    List<MenuItem> findByRestaurantId(UUID restaurantId);

    @EntityGraph(attributePaths = {"restaurant"})
    Page<MenuItem> findByRestaurantId(UUID restaurantId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MenuItem m WHERE m.id = :id")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<MenuItem> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT COUNT(m) > 0 FROM MenuItem m WHERE m.id = :id AND m.restaurant.id = :restaurantId")
    boolean existsByIdAndRestaurantId(@Param("id") UUID id, @Param("restaurantId") UUID restaurantId);

    @Query("SELECT mi FROM MenuItem mi JOIN FETCH mi.category WHERE mi.restaurant.id = :restaurantId")
    List<MenuItem> findByRestaurantIdWithCategories(@Param("restaurantId") UUID restaurantId);
}