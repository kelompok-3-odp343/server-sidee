package com.example.wandoor.repository;

import com.example.wandoor.model.entity.TrxCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrxCategoryRepository extends JpaRepository<TrxCategory, String> {
}