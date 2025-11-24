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
        SELECT COALESCE(tc.CATEGORY_NAME, 'Others') AS categoryName
        FROM WANDOOR.TRX_HISTORY t
        LEFT JOIN WANDOOR.TRX_CATEGORY tc ON tc.ID = t.CATEGORY_ID
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        GROUP BY COALESCE(tc.CATEGORY_NAME, 'Others')
        ORDER BY COUNT(*) DESC
        """, nativeQuery = true)
    List<String> fetchAllCategoryNamesForNasabah();

    @Query(value = """
        SELECT COUNT(*)
        FROM WANDOOR.TRX_HISTORY t
        LEFT JOIN WANDOOR.TRX_CATEGORY tc ON tc.ID = t.CATEGORY_ID
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
          AND COALESCE(tc.CATEGORY_NAME, 'Others') = :categoryName
        """, nativeQuery = true)
    Long countCategoryTransactionsForNasabah(String categoryName);

    @Query(value = """
        SELECT COUNT(*)
        FROM WANDOOR.TRX_HISTORY t
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        """, nativeQuery = true)
    Long countAllTransactionsForNasabah();

    @Query(value = """
        SELECT COALESCE(SUM(t.TRANSACTION_AMOUNT), 0)
        FROM WANDOOR.TRX_HISTORY t
        LEFT JOIN WANDOOR.TRX_CATEGORY tc ON tc.ID = t.CATEGORY_ID
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
          AND COALESCE(tc.CATEGORY_NAME, 'Others') = :categoryName
        """, nativeQuery = true)
    Double sumCategoryAmountForNasabah(String categoryName);

    @Query(value = """
        SELECT COALESCE(SUM(t.TRANSACTION_AMOUNT), 0)
        FROM WANDOOR.TRX_HISTORY t
        JOIN WANDOOR.USER_AUTH ua ON ua.USER_ID = t.USER_ID
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID
        WHERE rm.ROLE_NAME = 'NASABAH'
        """, nativeQuery = true)
    Double sumAllTransactionAmountForNasabah();

    @Query(value = """
        SELECT ua.USER_ID
        FROM WANDOOR.USER_AUTH ua
        JOIN WANDOOR.ROLE_MANAGEMENT rm ON rm.ID = ua.ROLE_ID AND rm.ROLE_NAME = 'NASABAH'
        ORDER BY ua.USER_ID
        """, nativeQuery = true)
    List<String> fetchAllNasabahUserIds();
}