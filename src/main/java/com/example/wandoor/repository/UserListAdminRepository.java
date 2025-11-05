package com.example.wandoor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.example.wandoor.model.entity.UserAuth;

@Repository
public interface UserListAdminRepository extends JpaRepository<UserAuth, String> {

    // 🔍 Cek role dari userId (untuk validasi admin)
    @Query("""
        SELECT rm.roleName
        FROM RoleManagement rm
        JOIN UserAuth ua ON rm.id = ua.roleId
        WHERE ua.id = :userId
    """)
    String findRoleByUserId(String userId);

    // 📋 Ambil daftar user dengan role NASABAH
    @Query("""
    SELECT ua.userId AS userId,
           CONCAT(p.firstName, ' ', COALESCE(p.middleName, ''), ' ', p.lastName) AS customerName,
           (SELECT COUNT(a.accountNumber) FROM Account a WHERE a.userId = ua.userId) AS countAccount,
           ua.isUserBlocked AS isBlocked
    FROM UserAuth ua
    JOIN Profile p ON p.id = ua.userId
    JOIN RoleManagement rm ON ua.roleId = rm.id
    WHERE rm.roleName = 'NASABAH'
    """)


    List<Object[]> findAllNasabahUserList();
}
