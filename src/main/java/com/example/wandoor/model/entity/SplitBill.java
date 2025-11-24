package com.example.wandoor.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class SplitBill {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    // @UuidGenerator
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String cif;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private  String splitBillTitle;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal totalAmount;

//    @Column(nullable = false)
//    private Integer hasPaid = 0;
//
//    @Column(nullable = false)
//    private LocalDateTime paymentTime;

    @Builder.Default
    @Column(nullable = false)
    private Integer isDeleted = 0;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdTime;

    @Column(nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedTime;
}
