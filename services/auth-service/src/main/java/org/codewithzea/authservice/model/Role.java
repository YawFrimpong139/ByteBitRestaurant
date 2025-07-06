package org.codewithzea.authservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, unique = true)
    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }
}
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//@Data
//@Entity
//@Table(name = "roles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//public class Role {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 20, unique = true, nullable = false)
//    @EqualsAndHashCode.Include
//    private RoleName name;
//
//    public enum RoleName {
//        ROLE_CUSTOMER,
//        ROLE_RESTAURANT_OWNER,
//        ROLE_ADMIN
//    }
//
//    public Role() {
//    }
//
//    public Role(RoleName name) {
//        this.name = name;
//    }
//}
