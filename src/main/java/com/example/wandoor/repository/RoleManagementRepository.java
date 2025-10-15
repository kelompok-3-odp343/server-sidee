package com.example.wandoor.repository;

import com.example.wandoor.model.entity.RoleManagement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleManagementRepository extends JpaRepository <RoleManagement, String>{
    Optional<RoleManagement> findFirstByUserId(String userid);
}
