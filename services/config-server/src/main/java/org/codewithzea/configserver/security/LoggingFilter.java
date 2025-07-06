package org.codewithzea.configserver.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath());

        return chain.filter(exchange)
                .doOnSuccess(v ->
                        logger.info("Completed request: {}", exchange.getResponse().getStatusCode())
                )
                .doOnError(e ->
                        logger.error("Failed request: {}", e.getMessage())
                );
    }

}
