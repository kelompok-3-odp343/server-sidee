package com.example.wandoor.repository;

import com.example.wandoor.model.entity.TrActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrActivityRepository extends JpaRepository <TrActivity, String> {
    @Override
    Optional<TrActivity> findById(String id);
    List<TrActivity> findByMakerId(String makerId);
    List<TrActivity> findByCheckerId(String checkerId);
    List<TrActivity> findByApproverId(String approverId);

    @Query("""
        select tra from TrActivity tra 
        where identifier = :identifier 
        and status in ('PENDING_CHECKER', 'PENDING_APPROVER')
        and menuId = :menuId
        """)
    Optional<TrActivity> findExistingActivity(
            @Param("identifier") String identifier,
            @Param("menuId") String menuId);

    @Query("""
            select t.id
            from TrActivity t
            where t.id like 'ACK%'
            order by t.id desc
            """)
    List<String> findLatestActivityIds();


}
