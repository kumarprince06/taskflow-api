package com.taskflow.service;

import com.taskflow.entity.RefreshToken;
import com.taskflow.entity.User;
import com.taskflow.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public RefreshToken createRefreshToken(User user){

        // Invalidate old Token if exists
        refreshTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        long refreshExpiration = 7 * 24 * 60 * 60 * 1000L; //7 days

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return (RefreshToken) refreshTokenRepository.save(refreshToken);
    }

    public boolean verifyExpiration(RefreshToken token){
        if (token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            return false;
        }

        return true;
    }

    @Transactional
    public void revokeToken(String tokenValue){
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }
}
