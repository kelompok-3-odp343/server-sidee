package com.example.wandoor.repository;

import com.example.wandoor.model.entity.SplitBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SplitBillRepository extends JpaRepository<SplitBill, String> {
    List<SplitBill> findByUserIdAndCif(String userId, String cif);
    Optional<SplitBill> findByTransactionId(String transactionId);
    Optional<SplitBill> findByIdAndUserIdAndCifAndTransactionId(String splitBillId, String userId, String Cif, String transactionId);
    Optional<SplitBill> findByUserIdAndCifAndId(String userId, String cif, String splitBillId);

    @Query("""
        SELECT COUNT(sb)
        FROM SplitBill sb
        WHERE sb.userId = :userId AND sb.cif = :cif AND sb.isDeleted = 0
    """)
    long countActiveByCreator(@Param("userId") String userId, @Param("cif") String cif);

    @Query("""
        SELECT COALESCE(SUM(sb.totalAmount), 0)
        FROM SplitBill sb
        WHERE sb.userId = :userId AND sb.cif = :cif AND sb.isDeleted = 0
    """)
    BigDecimal sumTotalBillByCreator(@Param("userId") String userId, @Param("cif") String cif);

    @Query("""
        SELECT * FROM  SplitBill sb
        JOIN sb.id ON SplitBillMember sbm
        ON sb.id = sbm.splitBillId
        WHERE sb.id = :splitBillId 
        AND sbm.id = :memberId 
        AND hasPaid = 0
    """)
    Optional<SplitBill> findUnpaidMember(@Param("splitBillId") String splitBillId, @Param("memberId") String memberId);


}
