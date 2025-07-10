package org.codewithzea.restaurantservice.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateRestaurantRequest(
        @NotBlank(message = "Restaurant name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2-100 characters")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid (e.g., user@example.com)")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(
                regexp = "^\\+?[0-9\\-\\s()]{7,20}$",
                message = "Phone number must be 7-20 digits and may include +, -, spaces, or parentheses"
        )
        String phone,

        @NotNull(message = "Address cannot be null")
        @Valid
        AddressRequest address
) {
    // Compact constructor for normalization
    public CreateRestaurantRequest {
        name = name != null ? name.trim() : null;
        email = email != null ? email.trim().toLowerCase() : null;
        phone = phone != null ? phone.replaceAll("[\\s()-]", "") : null;
    }
}