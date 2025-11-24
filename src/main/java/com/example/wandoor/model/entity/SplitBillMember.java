package com.example.wandoor.model.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class SplitBillMember {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    // @UuidGenerator
    @Column(nullable = false, updatable = false)
    private String id;

//    @Column(nullable = false)
//    private String splitBillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "split_bill_id", nullable = false)
    private SplitBill splitBill;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amountShare;

    @Builder.Default
    @Column(nullable = false)
    private Integer hasPaid = 0;

    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String memberName;

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
