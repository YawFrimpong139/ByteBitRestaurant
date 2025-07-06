package org.codewithzea.authservice.dto.response;


import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean enabled,
        boolean emailVerified,
        Instant createdAt,
        Instant updatedAt
) {}
