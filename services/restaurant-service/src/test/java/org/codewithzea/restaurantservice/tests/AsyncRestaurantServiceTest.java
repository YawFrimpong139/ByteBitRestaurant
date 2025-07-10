package org.codewithzea.restaurantservice.tests;


import org.codewithzea.restaurantservice.dto.request.RestaurantRequest;
import org.codewithzea.restaurantservice.dto.response.RestaurantResponse;
import org.codewithzea.restaurantservice.service.AsyncRestaurantService;
import org.codewithzea.restaurantservice.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class AsyncRestaurantServiceTest {

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private AsyncRestaurantService asyncRestaurantService;

    private final UUID restaurantId = UUID.randomUUID();

    // Success scenario tests
    @Test
    void getRestaurantAsync_ShouldReturnResponse_WhenRestaurantExists() throws Exception {
        // Arrange
        RestaurantResponse expected = createTestResponse();
        when(restaurantService.getRestaurantById(restaurantId))
                .thenReturn(expected);

        // Act
        CompletableFuture<RestaurantResponse> future =
                asyncRestaurantService.getRestaurantAsync(restaurantId);

        // Assert
        assertEquals(expected, future.get());
        verify(restaurantService).getRestaurantById(restaurantId);
    }

    @Test
    void updateRestaurantAsync_ShouldComplete_WhenValidRequest() throws Exception {
        // Arrange
        RestaurantRequest request = createTestRequest();
        doNothing().when(restaurantService).updateRestaurant(restaurantId, request);

        // Act
        CompletableFuture<Void> future =
                asyncRestaurantService.updateRestaurantAsync(restaurantId, request);

        // Assert
        assertNull(future.get());
        verify(restaurantService).updateRestaurant(restaurantId, request);
    }

    // Failure scenario tests
    @Test
    void getRestaurantAsync_ShouldCompleteExceptionally_WhenServiceThrows() {
        // Arrange
        when(restaurantService.getRestaurantById(restaurantId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<RestaurantResponse> future =
                asyncRestaurantService.getRestaurantAsync(restaurantId);

        // Assert
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void updateRestaurantAsync_ShouldCompleteExceptionally_WhenServiceThrows() {
        // Arrange
        RestaurantRequest request = createTestRequest();
        String errorMessage = "Update failed";

        // Mock to throw exception
        doThrow(new RuntimeException(errorMessage))
                .when(restaurantService).updateRestaurant(restaurantId, request);

        // Act
        CompletableFuture<Void> future =
                asyncRestaurantService.updateRestaurantAsync(restaurantId, request);

        // Assert
        assertTrue(future.isCompletedExceptionally());

        // Verify the exception content
        try {
            future.get();
            fail("Expected exception not thrown");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals(errorMessage, e.getCause().getMessage());
        }

        verify(restaurantService).updateRestaurant(restaurantId, request);
    }

    // Helper methods
    private RestaurantResponse createTestResponse() {
        return new RestaurantResponse(
                restaurantId.toString(),
                "Test Restaurant",
                "test@example.com",
                "1234567890",
                null,
                null,
                null,
                null
        );
    }

    private RestaurantRequest createTestRequest() {
        return new RestaurantRequest(
                "Test Restaurant",
                "test@example.com",
                "1234567890",
                null
        );
    }
}
//@ExtendWith(MockitoExtension.class)
//class AsyncRestaurantServiceTest {
//
//    @Mock
//    private RestaurantService restaurantService;
//
//    @InjectMocks
//    private AsyncRestaurantService asyncRestaurantService;
//
//    private final UUID restaurantId = UUID.randomUUID();
//
//    @Test
//    void getRestaurantAsync_ShouldCompleteWithResponse() throws Exception {
//        // Arrange
//        RestaurantResponse expectedResponse = new RestaurantResponse(
//                restaurantId.toString(),
//                "Test Restaurant",
//                "test@example.com",
//                "1234567890",
//                null,
//                null,
//                null,
//                null
//        );
//
//        when(restaurantService.getRestaurantById(restaurantId))
//                .thenReturn(expectedResponse);
//
//        // Act
//        CompletableFuture<RestaurantResponse> future =
//                asyncRestaurantService.getRestaurantAsync(restaurantId);
//
//        // Assert
//        assertEquals(expectedResponse, future.get());
//    }
//
//    @Test
//    void updateRestaurantAsync_ShouldCompleteSuccessfully() throws Exception {
//        // Arrange
//        RestaurantRequest request = new RestaurantRequest(
//                "Test Restaurant",
//                "test@example.com",
//                "1234567890",
//                null
//        );
//
//        // Act
//        CompletableFuture<Void> future =
//                asyncRestaurantService.updateRestaurantAsync(restaurantId, request);
//
//        // Assert
//        assertNull(future.get());
//        verify(restaurantService).updateRestaurant(restaurantId, request);
//    }
//}