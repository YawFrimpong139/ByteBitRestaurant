package org.codewithzea.restaurantservice.repository;


import jakarta.persistence.QueryHint;
import org.codewithzea.restaurantservice.model.Restaurant;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @EntityGraph(attributePaths = {"menuItems"})
    Optional<Restaurant> findWithMenuItemsById(UUID id);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenuItems(@Param("id") UUID id);

    @QueryHints(@QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true"))
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByIdAndOwnerId(UUID id, String ownerId);
}