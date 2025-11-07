package com.example.wandoor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.wandoor.model.entity.UserAuth;

@Repository
public interface UserListAdminRepository extends JpaRepository<UserAuth, String> {

    // ✅ Cek Role Langsung dari userId
    @Query("""
        SELECT rm.roleName
        FROM RoleManagement rm
        JOIN UserAuth ua ON rm.id = ua.roleId
        WHERE ua.userId = :userId
    """)
    String findRoleByUserId(String userId);


    // ✅ Summary Cepat & Aman (Oracle Friendly)
    @Query(value = """
    SELECT 
        (SELECT COUNT(*) 
         FROM USER_AUTH ua 
         JOIN ROLE_MANAGEMENT rm ON ua.ROLE_ID = rm.ID
         WHERE rm.ROLE_NAME = 'NASABAH') AS totalUsers,

        (SELECT COUNT(*) 
         FROM USER_AUTH ua 
         JOIN ROLE_MANAGEMENT rm ON ua.ROLE_ID = rm.ID
         WHERE rm.ROLE_NAME = 'NASABAH' AND ua.IS_USER_BLOCKED = 0) AS activeUsers,

        (SELECT COUNT(*) 
         FROM USER_AUTH ua 
         JOIN ROLE_MANAGEMENT rm ON ua.ROLE_ID = rm.ID
         WHERE rm.ROLE_NAME = 'NASABAH' AND ua.IS_USER_BLOCKED = 1) AS blockedUsers,

        (SELECT NVL(AVG(accCount), 0)
         FROM (
              SELECT COUNT(*) AS accCount
              FROM ACCOUNT a
              JOIN USER_AUTH ua2 ON a.USER_ID = ua2.USER_ID
              JOIN ROLE_MANAGEMENT rm2 ON ua2.ROLE_ID = rm2.ID
              WHERE rm2.ROLE_NAME = 'NASABAH'
              GROUP BY a.USER_ID
         )) AS avgAccountPerUser
    FROM DUAL
    """, nativeQuery = true)
    List<Object[]> getUserSummary();


    // ✅ Ambil List Data NASABAH
    @Query("""
    SELECT 
        ua.userId AS userId,
        p.id AS customerId, 
        CONCAT(
            CONCAT(CONCAT(p.firstName, ' '), COALESCE(p.middleName, '')),
            CONCAT(' ', p.lastName)
        ) AS customerName,
        (SELECT COUNT(a.accountNumber) FROM Account a WHERE a.userId = ua.userId) AS countAccount,
        ua.isUserBlocked AS isBlocked
    FROM UserAuth ua
    JOIN Profile p ON p.id = ua.userId
    JOIN RoleManagement rm ON ua.roleId = rm.id
    WHERE rm.roleName = 'NASABAH'
    """)
    List<Object[]> findAllNasabahUserList();

}
