package org.codewithzea.authservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Additional fields
    private String firstName;
    private String lastName;

    @Column(nullable = true, columnDefinition = "boolean default false")
    private boolean emailVerified = false;
}

//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.hibernate.annotations.NaturalId;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import lombok.EqualsAndHashCode;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.Instant;
//
//@Data
//@Entity
//@Table(name = "users")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//public class UserEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NaturalId
//    @Column(unique = true, nullable = false)
//    @EqualsAndHashCode.Include
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Column(nullable = false, length = 100)
//    private String name;
//
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "user_roles",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id"))
//    private Set<Role> roles = new HashSet<>();
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private Instant updatedAt;
//
//    public UserEntity() {
//    }
//
//    public UserEntity(String email, String password, String name) {
//        this.email = email;
//        this.password = password;
//        this.name = name;
//    }
//}
