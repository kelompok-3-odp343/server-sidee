package com.example.wandoor.repository;

import com.example.wandoor.model.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserListAdminRepository extends JpaRepository<UserAuth, String> {

    @Query("""
        SELECT rm.roleName
        FROM RoleManagement rm
        JOIN UserAuth ua ON rm.id = ua.roleId
        WHERE ua.userId = :userId
    """)
    String findRoleByUserId(String userId);

    @Query("""
        SELECT COUNT(ua)
        FROM UserAuth ua
        JOIN RoleManagement rm ON ua.roleId = rm.id
        WHERE rm.roleName = 'NASABAH'
    """)
    long countNasabahUsers();

    @Query("""
        SELECT COUNT(ua)
        FROM UserAuth ua
        JOIN RoleManagement rm ON ua.roleId = rm.id
        WHERE rm.roleName = 'NASABAH' AND (ua.isUserBlocked IS NULL OR ua.isUserBlocked = 0)
    """)
    long countActiveNasabahUsers();

    @Query("""
        SELECT COUNT(ua)
        FROM UserAuth ua
        JOIN RoleManagement rm ON ua.roleId = rm.id
        WHERE rm.roleName = 'NASABAH' AND ua.isUserBlocked = 1
    """)
    long countBlockedNasabahUsers();

    @Query("""
        SELECT ua
        FROM UserAuth ua
        JOIN RoleManagement rm ON ua.roleId = rm.id
        WHERE rm.roleName = 'NASABAH'
    """)
    List<UserAuth> findAllNasabahUsers();

}
