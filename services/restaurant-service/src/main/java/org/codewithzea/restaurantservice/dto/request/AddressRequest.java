package org.codewithzea.restaurantservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "Street address cannot be blank")
        @Size(max = 100, message = "Street address cannot exceed 100 characters")
        String street,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 50, message = "City name cannot exceed 50 characters")
        String city,

        @NotBlank(message = "State cannot be blank")
        @Size(min = 2, max = 2, message = "State must be 2-character abbreviation")
        String state,

        @NotBlank(message = "ZIP code cannot be blank")
        @Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$",
                message = "ZIP code must be in 5-digit or 9-digit format (12345 or 12345-6789)")
        String zipCode,

        @NotBlank(message = "Country cannot be blank")
        @Size(min = 2, max = 56, message = "Country name must be between 2-56 characters")
        String country
) {}