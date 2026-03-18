package com.fededev.cloudstorage.auth.response;

import com.fededev.cloudstorage.user.model.AppUser;

import java.util.UUID;

public record RotatedRefreshToken(
        UUID userId,
        String rawRefreshToken,
        AppUser user
) {
}
