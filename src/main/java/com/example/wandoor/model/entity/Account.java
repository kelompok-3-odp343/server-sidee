package com.example.wandoor.model.entity;

import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Account {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String cif;

    @Column(nullable = false)
    private BigDecimal effectiveBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType accountType;

    @Column(nullable = false)
    private String subCat;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String currencyCode;

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false)
    private Integer isMainAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private Integer isDeleted;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdTime;

    @Column(nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
