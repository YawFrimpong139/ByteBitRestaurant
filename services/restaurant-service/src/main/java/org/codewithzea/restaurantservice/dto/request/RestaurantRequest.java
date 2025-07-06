package org.codewithzea.restaurantservice.dto.request;


import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record RestaurantRequest(
        @NotBlank String name,
        @Email String email,
        @Pattern(regexp = "^\\+?[0-9\\-\\s]+$") String phone,
        AddressRequest address
) {}


