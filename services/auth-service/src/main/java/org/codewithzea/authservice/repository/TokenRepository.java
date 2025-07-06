package org.codewithzea.authservice.repository;


import org.codewithzea.authservice.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    @Query("""
        SELECT t FROM Token t 
        WHERE t.user.id = :userId 
        AND t.expired = false 
        AND t.revoked = false
    """)
    List<Token> findAllValidTokenByUser(Long userId);
}
