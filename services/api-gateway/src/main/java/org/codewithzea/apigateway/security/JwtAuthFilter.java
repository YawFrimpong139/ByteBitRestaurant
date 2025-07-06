package org.codewithzea.apigateway.security;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public JwtAuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String[] parts = authHeader.split(" ");
            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/api/auth/validate?token=" + parts[1])
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .flatMap(valid -> {
                        if (!valid) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-Authenticated-User", extractUsername(parts[1])) // Simplified
                                .build();
                        return chain.filter(exchange.mutate().request(request).build());
                    });
        };
    }

    private String extractUsername(String token) {
        // In production, use JWT parser to extract claims
        return "user"; // Simplified for example
    }

    public static class Config {
        // Put configuration properties here if needed
    }
}
