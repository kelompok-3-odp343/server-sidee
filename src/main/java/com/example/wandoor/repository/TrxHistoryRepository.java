package com.example.wandoor.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.wandoor.model.entity.TrxHistory;
import com.example.wandoor.model.enums.DebitCredit;

public interface TrxHistoryRepository extends JpaRepository<TrxHistory, String> {

    // ✅ FIXED: join ke Account agar bisa akses CIF
    @Query("""
        SELECT COALESCE(SUM(t.transactionAmount), 0)
        FROM TrxHistory t
        JOIN Account a ON t.accountNumber = a.accountNumber
        WHERE a.userId = :userId
          AND a.cif = :cif
          AND t.debitCredit = :debitCredit
    """)
    BigDecimal sumTransactionAmountByUserIdAndDebitCredit(
            @Param("userId") String userId,
            @Param("cif") String cif,
            @Param("debitCredit") DebitCredit debitCredit
    );

    // ✅ Masih bisa dipakai
    @Query("""
        SELECT COALESCE(SUM(t.transactionAmount), 0)
        FROM TrxHistory t
        WHERE t.userId = :userId
          AND t.debitCredit = :debitCredit
    """)
    BigDecimal sumTransactionAmountByUserIdAndDebitCredit(
            @Param("userId") String userId,
            @Param("debitCredit") DebitCredit debitCredit
    );

    Optional<TrxHistory> findByIdAndAccountNumber(String id, String accountNumber);

    @Query("""
        SELECT t FROM TrxHistory t
        WHERE t.userId = :userId
          AND t.accountNumber = :accountNumber
          AND EXTRACT(MONTH FROM t.transactionDate) = :month
          AND EXTRACT(YEAR FROM t.transactionDate) = :year
        ORDER BY t.transactionDate DESC
    """)
    List<TrxHistory> findByUserIdAndAccountNumberAndMonthYear(
            @Param("userId") String userId,
            @Param("accountNumber") String accountNumber,
            @Param("month") int month,
            @Param("year") int year
    );

    List<TrxHistory> findByAccountNumber(String accountNumber);
    Optional<TrxHistory> findById(String transactionId);
}
