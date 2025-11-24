package com.example.wandoor.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class TrxCategory {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "CATEGORY_NAME")
    private String categoryName;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "CREATED_TIME", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private LocalDateTime updatedTime;
}
