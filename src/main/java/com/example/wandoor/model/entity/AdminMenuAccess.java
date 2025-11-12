package com.example.wandoor.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Clob;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class AdminMenuAccess {
    @Id
    @UuidGenerator
    @Column(nullable = false)
    private String id;
    @Column(nullable = false)
    private String menuName;
    @Column(nullable = false)
    private String menuAction;
    @Column(nullable = false)
    private String actionFlow;
    @Column(nullable = false)
    private Integer reason;
    @Column(nullable = false)
    private LocalDateTime createdTime;
    @Column(nullable = false)
    private String createdBy;
    @Column(nullable = false)
    private LocalDateTime updatedTime;
    @Column(nullable = false)
    private String updatedBy;
}
