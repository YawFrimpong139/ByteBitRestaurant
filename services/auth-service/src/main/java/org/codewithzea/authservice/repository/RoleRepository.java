package org.codewithzea.authservice.repository;



import org.codewithzea.authservice.model.ERole;
import org.codewithzea.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);

    boolean existsByName(ERole role);
}