package com.example.wandoor.repository;

import com.example.wandoor.model.entity.SplitBillMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SplitBillMemberRepository extends JpaRepository<SplitBillMember, String> {
    // Remaining = jumlah porsi yang belum lunas pada SEMUA bill milik si creator
    @Query("""
        SELECT COALESCE(SUM(m.amountShare), 0)
        FROM SplitBillMember m
        JOIN m.splitBill sb
        WHERE sb.userId = :userId
          AND sb.cif = :cif
          AND sb.isDeleted = 0
          AND m.isDeleted = 0
          AND m.hasPaid = 0
    """)
    BigDecimal sumRemainingForCreator(@Param("userId") String userId, @Param("cif") String cif);

    List<SplitBillMember> findAllBySplitBillId(String splitBillId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE SplitBillMember sbm
        SET hasPaid = 1,
        updatedTime = CURRENT_TIMESTAMP,
        paymentDate = CURRENT_TIMESTAMP
        WHERE sbm.splitBill.id = :splitBillId
        AND sbm.id = :memberId
    """)
    int markAsPaid (@Param("splitBillId") String splitBillId, @Param("memberId") String memberId);

    @Query("""
            SELECT sbm
            FROM SplitBillMember sbm
            WHERE sbm.splitBill.id =:splitBillId
            AND sbm.id = :memberId
            AND sbm.hasPaid = 0
            """)
    Optional<SplitBillMember> findUnpaidSplitBillMemberById (@Param("splitBillId") String splitBillId, @Param("memberId") String memberId);
}
