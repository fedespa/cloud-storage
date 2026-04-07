package com.fededev.cloudstorage.sharing.model.response;

import com.fededev.cloudstorage.sharing.model.SharedLink;

import java.time.Instant;
import java.util.UUID;

public record SharedLinkDto(
        UUID id,
        String rawToken,
        Instant expiresAt,
        boolean revoked
) {
    public static SharedLinkDto from(SharedLink sharedLink, String rawToken) {
        return new SharedLinkDto(
                sharedLink.getId(),
                rawToken,
                sharedLink.getExpiresAt(),
                sharedLink.isRevoked()
        );
    }
}
