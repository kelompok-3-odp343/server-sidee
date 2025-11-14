package com.example.wandoor.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class AdminProfile {
    @Id
    @UuidGenerator
    @Column(nullable = false)
    private String id;
    @Column(nullable = false)
    private String npp;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String roleId;
    @Column(nullable = false)
    private String emailAdress;
    @Column(nullable = false)
    private LocalDateTime createdTime;
    @Column(nullable = false)
    private String createdBy;
    @Column(nullable = false)
    private LocalDateTime updatedTime;
    @Column(nullable = false)
    private String updatedBy;
}
