package com.example.wandoor.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Columns;
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
public class SplitBillMember {
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    @UuidGenerator
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
