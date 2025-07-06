package org.codewithzea.authservice.controller;

import org.codewithzea.authservice.dto.request.AuthRequest;
import org.codewithzea.authservice.dto.request.RegisterRequest;
import org.codewithzea.authservice.dto.request.TokenRefreshRequest;
import org.codewithzea.authservice.dto.response.AuthResponse;
import org.codewithzea.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with CUSTOMER role",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Authenticate user",
            description = "Returns JWT tokens for valid credentials",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates new access token using refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid refresh token")
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

}