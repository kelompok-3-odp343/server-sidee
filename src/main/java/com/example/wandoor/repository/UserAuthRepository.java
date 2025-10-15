package com.example.wandoor.repository;

import com.example.wandoor.model.entity.UserAuth;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
    Optional<UserAuth> findByUsername(String username);

//    @Modifying
//    @Query(
//            "UPDATE userAuth u SET u.isUserBlocked = 1 WHERE u.userId = :userId")
//    void blockUser(@Param("userId") String userId);

}
