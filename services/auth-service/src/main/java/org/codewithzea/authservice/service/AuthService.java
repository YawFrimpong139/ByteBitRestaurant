package org.codewithzea.authservice.service;


import org.codewithzea.authservice.dto.request.AuthRequest;
import org.codewithzea.authservice.dto.request.RegisterRequest;
import org.codewithzea.authservice.dto.request.TokenRefreshRequest;
import org.codewithzea.authservice.dto.response.AuthResponse;
import org.codewithzea.authservice.exception.*;
import org.codewithzea.authservice.model.*;
import org.codewithzea.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RoleService roleService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByName(ERole.ROLE_CUSTOMER));

        var user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .enabled(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Wrap the User in UserDetailsImpl before generating token
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        tokenService.saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return getAuthResponse(user);
    }

    private AuthResponse getAuthResponse(User user) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // Validate the refresh token first
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new TokenRefreshException(refreshToken, "Refresh token is invalid");
        }

        // Extract user email from the refresh token
        String userEmail = jwtService.extractUsername(refreshToken);

        // Find the user in database
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Create UserDetails from the user
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Generate new tokens
        var newAccessToken = jwtService.generateToken(userDetails);
        var newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Revoke old tokens and save new one
        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, newAccessToken);

        // Return the response
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}