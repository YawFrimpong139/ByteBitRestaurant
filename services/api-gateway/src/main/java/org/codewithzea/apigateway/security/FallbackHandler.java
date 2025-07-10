package org.codewithzea.apigateway.security;



import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class FallbackHandler {

    public Mono<ServerResponse> authServiceFallback() {
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        Map.of("message", "Auth service is unavailable. Please try again later.")
                ));
    }
}
