package com.example.wandoor.repository;

import com.example.wandoor.model.entity.SplitBill;
import com.example.wandoor.model.entity.TrxCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrxCategoryRepository extends JpaRepository<TrxCategory, String> {
    Optional<TrxCategory> findById(String id);
}
