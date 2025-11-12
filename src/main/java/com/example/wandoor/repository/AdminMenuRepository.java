package com.example.wandoor.repository;

import com.example.wandoor.model.entity.AdminMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminMenuRepository extends JpaRepository<AdminMenuAccess, String> {
}
