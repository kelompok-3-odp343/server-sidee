//adding imports
package com.example.wandoor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.wandoor.model.entity.UserAuth;

public interface TrxSummaryRepository extends JpaRepository<UserAuth, String> {

    @Query(value = """
        SELECT COALESCE(SUM(a.EFFECTIVE_BALANCE), 0)
        FROM WANDOOR.ACCOUNT a
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = a.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
          AND a.ACCOUNT_TYPE IN ('SVG','SAV')
        """, nativeQuery = true)
    Double sumSaving();

    @Query(value = """
        SELECT COALESCE(SUM(td.EFFECTIVE_BALANCE), 0)
        FROM WANDOOR.TIME_DEPOSIT_ACCOUNT td
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = td.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        """, nativeQuery = true)
    Double sumTimeDeposit();

    @Query(value = """
        SELECT COALESCE(SUM(lg.ACCOUNT_DEPOSIT), 0)
        FROM WANDOOR.LIFEGOALS_ACCOUNT lg
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = lg.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        """, nativeQuery = true)
    Double sumLifegoals();

    @Query(value = """
        SELECT COALESCE(SUM(dp.DPLK_INITIAL_DEPOSIT), 0)
        FROM WANDOOR.DPLK_ACCOUNT dp
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = dp.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        """, nativeQuery = true)
    Double sumPensionFund();

    @Query(value = """
        SELECT COALESCE(t.PAYMENT_METHOD, 'Others') AS categoryName,
               COALESCE(SUM(t.TRANSACTION_AMOUNT), 0) AS total
        FROM WANDOOR.TRX_HISTORY t
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        GROUP BY COALESCE(t.PAYMENT_METHOD, 'Others')
        ORDER BY categoryName
        """, nativeQuery = true)
    List<Object[]> fetchCategoryTotalsForNasabah();

    @Query(value = """
        SELECT ua.USER_ID AS userid,
               MIN(a.CIF) AS customerId,
               p.NIK AS nik,
               TRIM(p.FIRST_NAME || ' ' || COALESCE(p.MIDDLE_NAME, '') || ' ' || COALESCE(p.LAST_NAME, '')) AS customerName
        FROM WANDOOR.USER_AUTH ua
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID AND rm.ROLE_NAME = 'NASABAH'
        JOIN WANDOOR.PROFILE p ON p.ID = ua.USER_ID
        LEFT JOIN WANDOOR.ACCOUNT a ON a.USER_ID = ua.USER_ID
        GROUP BY ua.USER_ID, p.NIK, p.FIRST_NAME, p.MIDDLE_NAME, p.LAST_NAME
        ORDER BY customerName
        """, nativeQuery = true)
    List<Object[]> fetchAllNasabahUsers();
}