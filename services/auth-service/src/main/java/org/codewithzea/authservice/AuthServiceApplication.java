package org.codewithzea.authservice;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.codewithzea.authservice.service.RoleService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
//@EnableDiscoveryClient
@RequiredArgsConstructor
@EnableJpaAuditing
public class AuthServiceApplication {

    private final RoleService roleService;

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        roleService.initializeRoles();
    }
}
