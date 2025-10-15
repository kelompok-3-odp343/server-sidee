package com.example.wandoor.repository;

import com.example.wandoor.model.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpVerificationRepository extends JpaRepository<OtpVerification, String> {
    Optional<OtpVerification> findByIdAndIsUsed(String id, Integer isUsed);

}
