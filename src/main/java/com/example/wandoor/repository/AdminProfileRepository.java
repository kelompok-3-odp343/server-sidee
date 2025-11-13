package com.example.wandoor.repository;

import com.example.wandoor.model.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, String> {
    Optional<AdminProfile> findByRoleId(String roleId);
    Optional<AdminProfile> findById(String id);
}
