package org.codewithzea.authservice.service;


import org.codewithzea.authservice.exception.RoleNotFoundException;
import org.codewithzea.authservice.model.ERole;
import org.codewithzea.authservice.model.Role;
import org.codewithzea.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role findByName(ERole name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name.name()));
    }

    public void initializeRoles() {
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(role));
            }
        }
    }
}