package org.codewithzea.configserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for Eureka endpoints
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/", "/eureka/**").permitAll()
                                .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // Enables basic authentication

        return http.build();
    }
}