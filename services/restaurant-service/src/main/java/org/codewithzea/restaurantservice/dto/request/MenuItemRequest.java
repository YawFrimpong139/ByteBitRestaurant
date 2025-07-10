package org.codewithzea.restaurantservice.dto.request;



import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank(message = "Item name cannot be blank")
        @Size(min = 2, max = 100, message = "Item name must be between 2-100 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.01", message = "Price must be at least $0.01")
        @DecimalMax(value = "999.99", message = "Price cannot exceed $999.99")
        @Digits(integer = 3, fraction = 2, message = "Price must have up to 3 integer and 2 fraction digits")
        BigDecimal price
) {
    public MenuItemRequest {
        name = name != null ? name.trim() : null;
        description = description != null ? description.trim() : null;
    }
}

