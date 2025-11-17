package com.example.wandoor.repository;

import com.example.wandoor.model.entity.TrActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrActivityRepository extends JpaRepository <TrActivity, String> {
    @Override
    Optional<TrActivity> findById(String id);
    List<TrActivity> findByMakerId(String makerId);
    List<TrActivity> findByCheckerId(String checkerId);
    List<TrActivity> findByApproverId(String approverId);
}
