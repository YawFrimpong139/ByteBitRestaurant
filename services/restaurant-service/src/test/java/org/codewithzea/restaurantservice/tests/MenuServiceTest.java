package org.codewithzea.restaurantservice.tests;


import org.codewithzea.restaurantservice.dto.request.MenuItemRequest;
import org.codewithzea.restaurantservice.dto.response.MenuItemResponse;
import org.codewithzea.restaurantservice.event.MenuEventPublisher;
import org.codewithzea.restaurantservice.exception.*;
import org.codewithzea.restaurantservice.model.MenuItem;
import org.codewithzea.restaurantservice.model.Restaurant;
import org.codewithzea.restaurantservice.repository.MenuItemRepository;
import org.codewithzea.restaurantservice.repository.RestaurantRepository;
import org.codewithzea.restaurantservice.service.MenuService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuEventPublisher eventPublisher;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MenuService menuService;

    private final UUID restaurantId = UUID.randomUUID();
    private final UUID menuItemId = UUID.randomUUID();
    private final String ownerId = "user123";

    @BeforeEach
    void setUp() {

    }

    @Test
    void addMenuItem_ShouldSaveAndReturnItem_WhenRestaurantExists() {
        // Arrange
        UUID expectedItemId = UUID.randomUUID();
        Restaurant restaurant = createTestRestaurant();
        MenuItemRequest request = createTestRequest();
        setupSecurityContext(ownerId);

        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any(MenuItem.class)))
                .thenAnswer(invocation -> {
                    MenuItem item = invocation.getArgument(0);
                    item.setId(expectedItemId);
                    return item;
                });

        // Act
        MenuItemResponse response = menuService.addMenuItem(restaurantId, request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedItemId.toString(), response.id());
        assertEquals(request.name(), response.name());
        verify(menuItemRepository).save(any(MenuItem.class));
        verify(eventPublisher).publishMenuItemCreatedEvent(any(MenuItem.class));
        verifyNoMoreInteractions(menuItemRepository, eventPublisher);
    }

    @Test
    void addMenuItem_ShouldThrowException_WhenRestaurantNotFound() {
        // Arrange
        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RestaurantNotFoundException.class, () ->
                menuService.addMenuItem(restaurantId, createTestRequest()));

        verify(restaurantRepository).findById(restaurantId);
        verifyNoMoreInteractions(restaurantRepository);
        verifyNoInteractions(menuItemRepository, eventPublisher);
    }

    @Test
    void updateMenuItem_ShouldUpdate_WhenItemExistsAndBelongsToRestaurant() {
        // Arrange
        MenuItem menuItem = createTestMenuItem();
        Restaurant restaurant = createTestRestaurant();
        menuItem.setRestaurant(restaurant);
        setupSecurityContext(ownerId);

        when(menuItemRepository.findById(menuItemId))
                .thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any(MenuItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MenuItemResponse response = menuService.updateMenuItem(
                restaurantId, menuItemId, createTestRequest());

        // Assert
        assertNotNull(response);
        verify(menuItemRepository).save(any(MenuItem.class));
        verify(eventPublisher).publishMenuItemUpdatedEvent(any(MenuItem.class));
        verifyNoMoreInteractions(menuItemRepository, eventPublisher);
    }

    @Test
    void updateMenuItem_ShouldThrowException_WhenItemNotInRestaurant() {
        // Arrange
        MenuItem menuItem = createTestMenuItem();
        Restaurant otherRestaurant = createTestRestaurant();
        otherRestaurant.setId(UUID.randomUUID());
        menuItem.setRestaurant(otherRestaurant);

        when(menuItemRepository.findById(menuItemId))
                .thenReturn(Optional.of(menuItem));

        // Act & Assert
        assertThrows(MenuItemNotInRestaurantException.class, () ->
                menuService.updateMenuItem(restaurantId, menuItemId, createTestRequest()));
    }

    @Test
    void deleteMenuItem_ShouldDelete_WhenItemExistsAndBelongsToRestaurant() {
        // Arrange
        MenuItem menuItem = createTestMenuItem();
        Restaurant restaurant = createTestRestaurant();
        menuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(menuItemId))
                .thenReturn(Optional.of(menuItem));

        // Act
        menuService.deleteMenuItem(restaurantId, menuItemId);

        // Assert
        verify(menuItemRepository).delete(menuItem);
        verify(eventPublisher).publishMenuItemDeletedEvent(menuItemId, restaurantId);
        verifyNoMoreInteractions(menuItemRepository, eventPublisher);
    }

    @Test
    void addMenuItem_ShouldThrowException_WhenNotOwner() {
        // Arrange
        Restaurant restaurant = createTestRestaurant();
        restaurant.setOwnerId("otherUser"); // Different owner
        MenuItemRequest request = createTestRequest();

        // Setup security context with current user (different from owner)
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("currentUser");
        SecurityContextHolder.setContext(securityContext);

        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(restaurant));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                menuService.addMenuItem(restaurantId, request));

        // Verify
        verify(restaurantRepository).findById(restaurantId);
        verifyNoInteractions(menuItemRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void updateMenuItem_ShouldThrowException_WhenNotOwner() {
        // Arrange
        MenuItem menuItem = createTestMenuItem();
        Restaurant restaurant = createTestRestaurant();
        restaurant.setOwnerId("otherUser"); // Different owner
        menuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(menuItemId))
                .thenReturn(Optional.of(menuItem));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                menuService.updateMenuItem(restaurantId, menuItemId, createTestRequest()));

        verify(menuItemRepository).findById(menuItemId);
        verifyNoMoreInteractions(menuItemRepository);
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(menuItemRepository, eventPublisher);
    }

    @Test
    void deleteMenuItem_ShouldThrowException_WhenNotOwner() {
        // Arrange
        MenuItem menuItem = createTestMenuItem();
        Restaurant restaurant = createTestRestaurant();
        restaurant.setOwnerId("otherUser"); // Different owner
        menuItem.setRestaurant(restaurant);

        when(menuItemRepository.findById(menuItemId))
                .thenReturn(Optional.of(menuItem));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                menuService.deleteMenuItem(restaurantId, menuItemId));

        verify(menuItemRepository).findById(menuItemId);
        verifyNoMoreInteractions(menuItemRepository);
        verifyNoInteractions(eventPublisher);
        verifyNoMoreInteractions(menuItemRepository, eventPublisher);
    }

    private void setupSecurityContext(String username) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    private Restaurant createTestRestaurant() {
        return Restaurant.builder()
                .id(restaurantId)
                .name("Test Restaurant")
                .ownerId(ownerId)
                .build();
    }

    private MenuItem createTestMenuItem() {
        return MenuItem.builder()
                .id(menuItemId)
                .name("Test Item")
                .description("Test Description")
                .price(BigDecimal.valueOf(9.99))
                .build();
    }

    private MenuItemRequest createTestRequest() {
        return new MenuItemRequest(
                "Test Item",
                "Test Description",
                BigDecimal.valueOf(9.99)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
