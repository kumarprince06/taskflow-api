package com.taskflow.repository;

import com.taskflow.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository {

    Optional<RefreshToken> findByToken(String token);
    void deletedByUser_Id(Long UserId);
}
