package com.fededev.cloudstorage.auth.service;

import com.fededev.cloudstorage.auth.model.RefreshToken;
import com.fededev.cloudstorage.auth.repository.RefreshTokenRepository;
import com.fededev.cloudstorage.auth.response.RotatedRefreshToken;
import com.fededev.cloudstorage.common.exception.AppException;
import com.fededev.cloudstorage.common.exception.ErrorCode;
import com.fededev.cloudstorage.infraestructure.security.utils.HashUtils;
import com.fededev.cloudstorage.user.model.AppUser;
import com.fededev.cloudstorage.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final HashUtils hashUtils;
    private final UserRepository userRepository;
    private final RefreshTokenSecurityService refreshTokenSecurityService;

    @Value("${refresh.token.expiration}")
    private long expiration;

    public String create(UUID id) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = this.hashUtils.sha256(rawToken);

        AppUser userProxy = this.userRepository.getReferenceById(id);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(userProxy)
                .token(hashedToken)
                .expiresAt(Instant.now().plusSeconds(this.expiration))
                .build();

        this.refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RotatedRefreshToken rotateToken(String rawToken) {

        String hashedToken = this.hashUtils.sha256(rawToken);

        RefreshToken oldToken = this.refreshTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

        UUID userId = oldToken.getUser().getId();

        if (oldToken.isRevoked()) {
            this.refreshTokenSecurityService.handleTokenReuse(userId);
            throw new AppException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        if (oldToken.isExpired()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        oldToken.setRevoked(true);
        this.refreshTokenRepository.save(oldToken);

        String newRefreshToken = create(userId);

        return new RotatedRefreshToken(
                userId,
                newRefreshToken,
                oldToken.getUser()
        );
    }

}
