package org.codewithzea.restaurantservice.dto.request;



import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank String name,
        String description,
        @DecimalMin("0.01") BigDecimal price
) {}

