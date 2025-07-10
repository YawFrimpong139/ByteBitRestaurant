package org.codewithzea.restaurantservice.integrationtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codewithzea.restaurantservice.dto.request.CreateRestaurantRequest;
import org.codewithzea.restaurantservice.dto.request.AddressRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RestaurantControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @WithMockUser(roles = "ADMIN", authorities = "SCOPE_api:write")
    void shouldCreateRestaurant() throws Exception {
        // Given
        AddressRequest address = new AddressRequest(
                "123 Main St",
                "Foodville",
                "FV",
                "12345",
                "USA"
        );

        CreateRestaurantRequest request = new CreateRestaurantRequest(
                "New Restaurant",
                "contact@newrestaurant.com",
                "+1 (555) 123-4567",
                address
        );

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Restaurant"))
                .andExpect(jsonPath("$.email").value("contact@newrestaurant.com"))
                .andExpect(jsonPath("$.phone").exists())
                .andExpect(jsonPath("$.address.street").value("123 Main St"));
    }
}