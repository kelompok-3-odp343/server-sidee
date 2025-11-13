package com.example.wandoor.repository;

import com.example.wandoor.model.entity.Account;
import com.example.wandoor.model.enums.AccountStatus;
import com.example.wandoor.model.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUserIdAndCif(String userId, String cif);
    Optional<Account> findByUserIdAndCifAndAccountNumber(String userId, String cif, String accountNumber);

    @Modifying
    @Query("""
        SELECT a FROM Account a
         WHERE a.cif = :cif
           AND a.userId = :userId
           AND a.isDeleted = 0
           AND a.accountStatus IN :statuses
         ORDER BY CASE WHEN a.isMainAccount = 1 THEN 0 ELSE 1 END,
                  a.createdTime DESC
        """)
        List<Account> fetchActiveAccounts(@Param("userId") String userId,
                                          @Param("cif") String cif,
                                          @Param("statuses") Collection<AccountStatus> statuses);

    List<Account> findByUserIdAndCifAndAccountType(String userId, String cif, ProductType productType);

}
