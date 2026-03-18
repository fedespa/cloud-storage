package com.fededev.cloudstorage.auth.service;

import com.fededev.cloudstorage.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenSecurityService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTokenReuse(UUID userId) {
        log.warn("Alerta, intento de reutilización de Refresh Token para el usuario: {}", userId);
        this.refreshTokenRepository.revokeAllUserTokens(userId);
    }

}