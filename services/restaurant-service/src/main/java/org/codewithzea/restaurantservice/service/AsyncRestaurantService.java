package org.codewithzea.restaurantservice.service;

import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncRestaurantService {
    private static final Logger log = LoggerFactory.getLogger(AsyncRestaurantService.class);

    private final RestaurantService restaurantService;

    public AsyncRestaurantService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Async
    public CompletableFuture<RestaurantResponse> getRestaurantAsync(UUID id) {
        MDC.put("operation", "asyncGetRestaurant");
        MDC.put("restaurantId", id.toString());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("Starting async restaurant retrieval");

        try {
            RestaurantResponse response = restaurantService.getRestaurantById(id);
            log.debug("Successfully retrieved restaurant asynchronously");
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Async restaurant retrieval failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            stopWatch.stop();
            log.info("Async operation completed in {} ms", stopWatch.getTotalTimeMillis());
            MDC.clear();
        }
    }

    @Async
    public CompletableFuture<Void> updateRestaurantAsync(UUID id, RestaurantRequest request) {
        MDC.put("operation", "asyncUpdateRestaurant");
        MDC.put("restaurantId", id.toString());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        log.info("Starting async restaurant update for {}", request.name());

        try {
            restaurantService.updateRestaurant(id, request);
            log.debug("Successfully completed async update");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async restaurant update failed: {}", e.getMessage(), e);
            throw e;
        } finally {
            stopWatch.stop();
            log.info("Async update completed in {} ms", stopWatch.getTotalTimeMillis());
            MDC.clear();
        }
    }

    @Async
    public CompletableFuture<Void> processBatchUpdateAsync(List<RestaurantUpdateTask> tasks) {
        MDC.put("operation", "batchUpdateRestaurants");
        MDC.put("batchSize", String.valueOf(tasks.size()));
        StopWatch batchStopWatch = new StopWatch();
        batchStopWatch.start();

        log.info("Starting batch update of {} restaurants", tasks.size());

        try {
            List<CompletableFuture<Void>> futures = tasks.stream()
                    .map(task -> {
                        MDC.put("currentRestaurantId", task.id().toString());
                        log.debug("Processing update for restaurant {}", task.id());
                        return updateRestaurantAsync(task.id(), task.request())
                                .exceptionally(e -> {
                                    log.warn("Failed to update restaurant {}: {}", task.id(), e.getMessage());
                                    return null;
                                });
                    })
                    .toList();

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        } finally {
            batchStopWatch.stop();
            log.info("Batch update completed in {} ms", batchStopWatch.getTotalTimeMillis());
            MDC.clear();
        }
    }

    public record RestaurantUpdateTask(UUID id, RestaurantRequest request) {}
}