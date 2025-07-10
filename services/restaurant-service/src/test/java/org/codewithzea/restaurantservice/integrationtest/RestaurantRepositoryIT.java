package org.codewithzea.restaurantservice.integrationtest;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.reactor.IOSession;
//import org.apache.hc.core5.reactor.IOSession;
import lombok.extern.slf4j.Slf4j;
import org.codewithzea.restaurantservice.model.Address;
import org.codewithzea.restaurantservice.model.Restaurant;
import org.codewithzea.restaurantservice.model.RestaurantStatus;
import org.codewithzea.restaurantservice.repository.RestaurantRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rocksdb.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Slf4j
class RestaurantRepositoryIT {

    @BeforeAll
    static void checkDocker() {
        Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available"
        );
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("restaurant_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistRestaurantWithAllFields() {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .name("Test Restaurant")
                .email("test@example.com")
                .phone("1234567890")
                .ownerId("auth0|12345")
                .status(RestaurantStatus.ACTIVE)
                .address(Address.builder()
                        .street("123 Main St")
                        .city("Foodville")
                        .state("FV")
                        .zipCode("12345")
                        .build())
                .build();

        // When
        Restaurant saved = restaurantRepository.save(restaurant);
        entityManager.flush();
        entityManager.clear();

        // Then
        Restaurant found = restaurantRepository.findById(saved.getId()).orElseThrow();

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test Restaurant");
        assertThat(found.getStatus()).isEqualTo(RestaurantStatus.ACTIVE);
        assertThat(found.getAddress().getCity()).isEqualTo("Foodville");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindByStatus() {
        // Given
        Restaurant active = createTestRestaurant("Active", RestaurantStatus.ACTIVE);
        Restaurant inactive = createTestRestaurant("Inactive", RestaurantStatus.INACTIVE);
        restaurantRepository.saveAll(List.of(active, inactive));

        // When
        List<Restaurant> activeRestaurants = restaurantRepository.findByStatus(RestaurantStatus.ACTIVE);

        // Then
        assertThat(activeRestaurants)
                .hasSize(1)
                .extracting(Restaurant::getName)
                .containsExactly("Active");
    }

    private Restaurant createTestRestaurant(String name, RestaurantStatus status) {
        return Restaurant.builder()
                .name(name)
                .email(name.toLowerCase() + "@test.com")
                .phone("1234567890")
                .ownerId("owner_" + name)
                .status(status)
                .address(Address.builder()
                        .street(name + " Street")
                        .city("Test City")
                        .state("TC")
                        .zipCode("12345")
                        .build())
                .build();
    }
}