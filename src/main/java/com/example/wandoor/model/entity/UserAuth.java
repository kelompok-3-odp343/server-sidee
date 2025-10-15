package com.example.wandoor.model.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.NumericBooleanConverter;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UserAuth {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private  String username;

    @Column(nullable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String roleId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer isUserBlocked;
}
