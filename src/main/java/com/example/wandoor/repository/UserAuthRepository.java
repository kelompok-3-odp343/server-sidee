package com.example.wandoor.repository;

import com.example.wandoor.model.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, String> {
    Optional<UserAuth> findByUsername(String username);

//    @Modifying
//    @Query(
//            "UPDATE userAuth u SET u.isUserBlocked = 1 WHERE u.userId = :userId")
//    void blockUser(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserAuth u set u.isUserBlocked = 1 where u.userId = :userId and (u.isUserBlocked is null or u.isUserBlocked <> 1)")
    int markBlockedById(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserAuth u set u.isUserBlocked = 0 where u.userId = :userId and (u.isUserBlocked is null or u.isUserBlocked <> 0)")
    int markUnblockedById(@Param("userId") String userId);

}
