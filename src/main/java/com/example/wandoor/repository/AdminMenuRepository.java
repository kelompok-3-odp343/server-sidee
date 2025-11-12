package com.example.wandoor.repository;

import com.example.wandoor.model.entity.MsMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminMenuRepository extends JpaRepository<MsMenu, String> {
}
