package org.codewithzea.authservice.dto.request;



import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}


//
//import jakarta.validation.constraints.NotBlank;
//import lombok.Data;
//
//@Data
//public class TokenRefreshRequest {
//    @NotBlank
//    private String refreshToken;
//}
