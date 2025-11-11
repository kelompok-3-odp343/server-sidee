package com.example.wandoor.repository;

import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailUserAdminRepository extends JpaRepository<Account, String> {

    @Query(value = """
        SELECT 
            ua.USER_ID AS userId,
            ua.IS_USER_BLOCKED AS isBlocked,
            p.FIRST_NAME AS firstName,
            p.MIDDLE_NAME AS middleName,
            p.LAST_NAME AS lastName,
            a.ACCOUNT_NUMBER AS accountNumber,
            a.ACCOUNT_TYPE AS productType,
            a.PRODUCT_NAME AS productName,
            a.ACCOUNT_STATUS AS accountStatus,
            a.EFFECTIVE_BALANCE AS effectiveBalance,
            a.CIF AS customerId
        FROM USER_AUTH ua
        JOIN PROFILE p ON p.ID = ua.USER_ID
        JOIN ACCOUNT a ON a.USER_ID = ua.USER_ID
        JOIN ROLE_MANAGEMENT rm ON ua.ROLE_ID = rm.ID
        WHERE rm.ROLE_NAME = 'NASABAH'
          AND ua.USER_ID = :userId
        """, nativeQuery = true)
    List<Object[]> findNasabahDetailByUserId(@Param("userId") String userId);
}
